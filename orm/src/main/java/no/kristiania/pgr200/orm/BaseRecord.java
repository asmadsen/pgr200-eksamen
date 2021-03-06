package no.kristiania.pgr200.orm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import no.kristiania.pgr200.orm.annotations.Relation;
import no.kristiania.pgr200.orm.generics.Listable;
import no.kristiania.pgr200.orm.relations.AbstractRelation;
import no.kristiania.pgr200.orm.relations.BelongsTo;
import no.kristiania.pgr200.orm.relations.HasMany;
import no.kristiania.pgr200.orm.relations.HasOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

public abstract class BaseRecord<
        T extends BaseRecord<T, S>,
        S extends IBaseModel<S>> {
    private static Logger logger = LoggerFactory.getLogger(BaseRecord.class);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private S state;
    private S dbState;

    protected Map<String, Object> relations = new HashMap<>();

    public BaseRecord(Class<S> modelClass) {
        try {
            this.state = modelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Constructor", e);
        }
    }

    public BaseRecord(S state) {
        setState(state);
        if (getState().getAttributes().get(getPrimaryKey()) != null &&
                getState().getAttributes().get(getPrimaryKey()).getValue() != null) {
            T dbState = findById((UUID) getState().getAttributes().get(getPrimaryKey()).getValue());
            if (dbState != null) setDbState(dbState.getState());
        }
    }

    public abstract String getTable();

    public String getPrimaryKey() {
        return "id";
    }

    public T newModelInstance() {
        try {
            return (T) getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("newModelInstance", e);
        }
        return null;
    }


    public S newStateInstance() {
        return this.state.newStateInstance();
    }

    public boolean save() {
        if (isDirty()) {
            if (exists()) {
                if (!update()) return false;
            } else {
                if (!create()) return false;
            }
        }
        return true;
    }

    public boolean update() {
        if (isDirty()) {
            UpdateQuery<T> updateQuery = new UpdateQuery<>((T) this).whereEquals(getPrimaryKey(),
                                                                                 state.getAttribute(getPrimaryKey())
                                                                                      .getValue());
            state.getAttributes().forEach((k, v) -> {
                if (v != null) updateQuery.set(k, v.getValue());
            });
            try {
                if (updateQuery.get() > 0) {
                    setDbState(newStateInstance().withAttributes(state.getAttributes()));
                    return true;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                logger.warn(e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean create() {
        state.setAttribute(getPrimaryKey(), UUID.randomUUID());
        InsertQuery insertQuery = new InsertQuery(getTable()).insert(this);
        try {
            if (insertQuery.get() > 0) {
                setDbState(newStateInstance().withAttributes(state.getAttributes()));
                return true;
            }
        } catch (SQLException e) {
            logger.error("create", e);
        }
        return false;
    }

    public boolean create(Map<String, ColumnValue> attributes) {
        state.populateAttributes(attributes);
        return create();
    }

    public boolean destroy() {
        if (exists()) {
            DeleteQuery deleteQuery = new DeleteQuery<>(getTable()).whereEquals("id",
                                                                                state.getAttributes()
                                                                                     .get(getPrimaryKey())
                                                                                     .getValue());
            try {
                if (deleteQuery.get() > 0) {
                    setDbState(null);
                    return true;
                }
            } catch (SQLException e) {
                logger.error("destroy", e);
            }
        }
        return false;
    }

    public final List<T> all() {
        return this.newQuery().get();
    }

    public final T findById(UUID id) {
        return this.newQuery()
                   .whereEquals("id", id).first();
    }

    public SelectQuery<T, S> newQuery() {
        return new SelectQuery<>((T) this, state.getAttributes().keySet().toArray(new String[0]));
    }


    public BaseRecord<T, S> fill(Map<String, ColumnValue> attributes) {
        this.state.populateAttributes(attributes);
        return this;
    }

    private boolean exists() {
        return getState().getAttributes().get(getPrimaryKey()) != null;
    }

    public boolean isDirty() {
        return !this.getState().equals(getDbState());
    }

    public S getState() {
        return state;
    }

    protected void setState(S state) {
        this.state = state;
    }

    public S getDbState() {
        return dbState;
    }

    public void setDbState(S dbState) {
        this.dbState = dbState;
    }

    public void setRelation(String relation, Object value) {
        this.relations.put(relation, value);
    }

    public <V extends BaseRecord<V, ?>> Listable<V> getRelation(String relation, Class<V> returnType) {
        if (!this.relations.containsKey(relation)) {
            this.loadRelation(relation);
        }

        Object relationValue = this.relations.get(relation);
        if (relationValue != null) {
            if (relationValue instanceof Collection) {
                return new Listable<>((Collection<V>) relationValue);
            }
            return new Listable<>((V) relationValue);
        }
        return new Listable<>((V) null);
    }

    private <N extends IBaseModel<N>, M extends BaseRecord<M, N>> void loadRelation(String relationName) {
        AbstractRelation<M, N, T> relation = (AbstractRelation<M, N, T>) this.getRelations().get(relationName);
        if (relation == null) {
            throw new IllegalArgumentException("Tried to load undefined relation `" + relationName + "`");
        }
        relation.addConstraints();
        List<T> models = new LinkedList<>();
        models.add((T) this);
        List<T> match = relation.match(models, relation.getEager().getListValue(), relationName);
        this.setRelation(relationName, match.get(0).relations.get(relationName));
    }

    public Map<String, AbstractRelation<?, ?, T>> getRelations() {
        Map<String, AbstractRelation<?, ?, T>> relations = new HashMap<>();
        for (Method declaredMethod : getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(Relation.class)) continue;
            //if (!declaredMethod.getReturnType().isInstance(AbstractRelation.class)) continue;
            try {
                AbstractRelation<?, ?, T> relation = (AbstractRelation<?, ?, T>) declaredMethod.invoke(this);
                relations.put(
                        declaredMethod.getName(),
                        relation
                );
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("getRelations", e);
            }
        }
        return relations;
    }

    public ColumnValue<?> getColumnValue(String column) {
        return this.getState().getAttribute(column);
    }

    public UUID getPrimaryKeyValue() {
        return (UUID) this.getColumnValue(this.getPrimaryKey()).getValue();
    }

    protected <V extends BaseRecord<V, W>, W extends IBaseModel<W>> HasOne<V, W, T> hasOne(V relation, String foreignKey, String localKey) {
        return new HasOne<>(relation, (T) this, foreignKey, localKey);
    }

    protected <V extends BaseRecord<V, W>, W extends IBaseModel<W>> BelongsTo<V, W, T> belongsTo(V relation, String foreignKey, String ownerKey) {
        return new BelongsTo<>(relation, (T) this, foreignKey, ownerKey);
    }

    protected <V extends BaseRecord<V, W>, W extends IBaseModel<W>> HasMany<V, W, T> hasMany(V relation, String foreignKey, String localKey) {
        return new HasMany<>(relation, (T) this, foreignKey, localKey);
    }

    public JsonObject toJson() {
        JsonObject object = BaseRecord.gson.toJsonTree(this.getState()).getAsJsonObject();
        for (Map.Entry<String, Object> entry : this.relations.entrySet()) {
            if (entry.getValue() instanceof Collection) {
                JsonArray children = new JsonArray();
                for (BaseRecord value : ((Collection<BaseRecord>) entry.getValue())) {
                    children.add(value.toJson());
                }
                object.add(entry.getKey(), children);
            } else {
                if (entry.getValue() != null) object.add(entry.getKey(), ((BaseRecord) entry.getValue()).toJson());
                else object.add(entry.getKey(), null);
            }
        }
        return object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getState(), this.getDbState());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseRecord) {
            return this.getState().equals(((BaseRecord) obj).getState()) &&
                    (this.getDbState() == null ?
                            ((BaseRecord) obj).getDbState() == null :
                            this.getDbState().equals(((BaseRecord) obj).getDbState()));
        }
        return false;
    }
}
