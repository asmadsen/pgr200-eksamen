package no.kristiania.pgr200.common.Models;

import no.kristiania.pgr200.common.Utils.Utils;
import no.kristiania.pgr200.orm.ColumnValue;
import no.kristiania.pgr200.orm.IBaseModel;

import javax.validation.ConstraintViolation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseModel<T> implements IBaseModel<T> {
    @Override
    public Set<ConstraintViolation<T>> validate() {
        return Utils.validator().validate((T) this);
    }


    @Override
    public T newStateInstance() {
        try {
            return (T) getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void populateAttributes(Map<String, ColumnValue> attributes) {
        Class thisClass = getClass();
        for (Map.Entry<String, ColumnValue> entry : attributes.entrySet()) {
            try {
                Field field = thisClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(this, entry.getValue().getValue());
            } catch (NoSuchFieldException | IllegalAccessException ignored) { }
        }
    }

    @Override
    public Map<String, ColumnValue> getAttributes() {
        Map<String, ColumnValue> attributes = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            if(field.isSynthetic()) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if(value != null) value = new ColumnValue<>(field.get(this));
                attributes.put(field.getName(), (ColumnValue) value);
            } catch (IllegalAccessException ignored) { }
        }
        return attributes;
    }

    @Override
    public void setAttribute(String column, Object value) {
        try {
            getClass().getDeclaredField(column).set(this, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ColumnValue getAttribute(String column) {
        try {
            return new ColumnValue<>(getClass().getDeclaredField(column).get(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract boolean equals(Object other);
    public abstract int hashCode();
}
