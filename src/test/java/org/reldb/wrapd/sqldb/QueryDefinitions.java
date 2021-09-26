package org.reldb.wrapd.sqldb;

public class QueryDefinitions extends Definer {

    private String queryName;

    public QueryDefinitions(Database database, String codeDirectory, String pkg, String queryName) {
        super(database, codeDirectory, pkg);
        this.queryName = queryName;
    }

    public void generate() throws Throwable {
        defineQuery(queryName + "Query01", "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
        defineQuery(queryName + "Query02", "SELECT * FROM $$tester");
        defineQuery(queryName + "Query03", "SELECT * FROM $$tester WHERE x > ?", 3);
        defineQuery(queryName + "Query04", "SELECT x FROM $$tester");
    }
}
