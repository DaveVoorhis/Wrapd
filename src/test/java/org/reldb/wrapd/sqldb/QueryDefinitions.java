package org.reldb.wrapd.sqldb;

public class QueryDefinitions extends QueryDefiner {

    private String queryName;

    public QueryDefinitions(Database database, String codeDirectory, String queryName) {
        super(database, codeDirectory);
        this.queryName = queryName;
    }

    public QueryDefinition QueryDefinition01() {
        return new QueryDefinition(queryName + "Query01", "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
    }

    public QueryDefinition QueryDefinition02() {
        return new QueryDefinition(queryName + "Query02", "SELECT * FROM $$tester");
    }
}
