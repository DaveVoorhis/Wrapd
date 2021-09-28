package org.reldb.wrapd.sqldb;

public class QueryDefinitions extends Definer {

    private String queryName;

    public QueryDefinitions(Database database, String codeDirectory, String pkg) {
        super(database, codeDirectory, pkg);
    }

    public void generate() throws Throwable {
        defineQuery("Query01", "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
        defineQuery("Query02", "SELECT * FROM $$tester");
        defineQuery("Query03", "SELECT * FROM $$tester WHERE x > ?", 3);
        defineQuery("Query04", "SELECT x FROM $$tester");

        defineTable("$$abc");
        defineTable("$$xyz", "x = ?", 22);
        defineQueryForTable("ABCWhere", "$$abc", "SELECT * FROM $$abc WHERE a = ?", 22);
        defineQuery("ABCJoinXYZ", "SELECT * FROM $$abc, $$xyz WHERE x = a");
        defineQuery("ABCJoinXYZWhere", "SELECT * FROM $$abc, $$xyz WHERE x = a AND x > ? AND x < ?", 2, 5);
        defineUpdate("ClearABC", "DELETE FROM $$abc");
        defineUpdate("ClearXYZ", "DELETE FROM $$xyz");
        defineUpdate("ClearABCWhere", "DELETE FROM $$abc WHERE a = ?", 3);
    }
}
