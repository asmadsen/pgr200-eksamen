package no.kristiania.pgr200.orm.enums;

public enum Statement {
    SELECT("SELECT"),
    DISTINCT("DISTINCT"),
    INSERT("INSERT INTO"),
    VALUES("VALUES"),
    UPDATE("UPDATE"),
    DELETE("DELETE FROM"),
    SET("SET"),
    FROM("FROM"),
    JOIN("JOIN"),
    ON("ON"),
    WHERE("WHERE"),
    GROUP_BY("GROUP BY"),
    HAVING("HAVING"),
    ORDER_BY("ORDER BY");

    private final String statement;

    Statement(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }
}
