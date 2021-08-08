package org.reldb.wrapd.sqldb;

public class QueryDefinitions extends QueryDefiner {

    private String queryName;

    public QueryDefinitions(Database database, String codeDirectory, String pkg, String queryName) {
        super(database, codeDirectory, pkg);
        this.queryName = queryName;
    }

    public QueryDefinition QueryDefinition01() {
        return new QueryDefinition(queryName + "Query01", "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
    }

    public QueryDefinition QueryDefinition02() {
        return new QueryDefinition(queryName + "Query02", "SELECT * FROM $$tester");
    }

    public void QueryDefinition03() throws Exception {
        define(queryName + "Query03", "SELECT * FROM $$tester WHERE x > ?", 3);
    }

    public void queryDefinition04() throws Exception {
        define(queryName + "Query04", "SELECT x FROM $$tester");
    }
}
