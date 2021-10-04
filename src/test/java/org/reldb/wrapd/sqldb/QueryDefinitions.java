package org.reldb.wrapd.sqldb;

public class QueryDefinitions extends Definer {

    private String queryName;

    public QueryDefinitions(Database database, String codeDirectory, String pkg) {
        super(database, codeDirectory, pkg);
    }

    public void generate() throws Throwable {
        defineTable("$$abc");
        defineTable("$$xyz", "x = ?", 22);
        defineQueryForTable("ABCWhere", "$$abc", "SELECT * FROM $$abc WHERE a = ?", 22);
        defineQuery("ABCJoinXYZ", "SELECT * FROM $$abc, $$xyz WHERE x = a");
        defineQuery("ABCJoinXYZWhere", "SELECT * FROM $$abc, $$xyz WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
        defineUpdate("ClearABC", "DELETE FROM $$abc");
        defineUpdate("ClearXYZ", "DELETE FROM $$xyz");
        defineUpdate("ClearABCWhere", "DELETE FROM $$abc WHERE a = {aValue}", 3);
    }
}
