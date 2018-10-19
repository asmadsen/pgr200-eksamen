package no.kristiania.pgr200.orm;


import no.kristiania.pgr200.orm.Enums.JoinType;

public class JoinStatement<T extends BaseRecord> {
    private T model;
    private Query query;
    private String foreignKey;
    private String localKey;
    private String alias;
    private JoinType type;

    public JoinStatement(T model, String foreignKey, String localKey, JoinType joinType) {
        this.model = model;
        this.foreignKey = foreignKey;
        this.localKey = localKey;
        this.type = joinType;
    }

    public JoinStatement(T model, String foreignKey, String localKey) {
        this(model, foreignKey, localKey, JoinType.LeftJoin);
    }

    public JoinStatement(Query<T> query, String alias, String foreignKey, String localKey, JoinType joinType) {
        this.query = query;
        this.alias = alias;
        this.foreignKey = foreignKey;
        this.localKey = localKey;
        this.type = joinType;
    }

    public JoinStatement(Query<T> query, String alias, String foreignKey, String localKey) {
        this(query, alias, foreignKey, localKey, JoinType.LeftJoin);
    }


    public String getSqlStatement(String table) {
        if (this.query != null) {
            return String.format(
                    "%s (%s) ON `%s`.`%s` = `%s`.`%s`",
                    this.type.getSql(),
                    this.query.getSqlStatement(),
                    this.alias,
                    this.foreignKey,
                    table,
                    this.localKey
            );
        }
        return String.format(
                "%s `%s` ON `%s`.`%s` = `%s`.`%s`",
                this.type.getSql(),
                this.model.getTable(),
                this.model.getTable(),
                this.foreignKey,
                table,
                this.localKey
        );
    }

}