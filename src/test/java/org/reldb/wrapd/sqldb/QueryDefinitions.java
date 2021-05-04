package org.reldb.wrapd.sqldb;

public class QueryDefinitions extends QueryDefiner {

    public QueryDefinitions(Database database, String codeDirectory) {
        super(database, codeDirectory);
    }

    public QueryDefinition QueryDefinition01() {
        return new QueryDefinition("Query01", "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
    }

    public QueryDefinition QueryDefinition02() {
        return new QueryDefinition("Query02", "SELECT * FROM $$tester");
    }
}
