package org.reldb.wrapd.sqldb;

/**
 * These query definitions should be valid, as they will be run to test them.
 */
public class QueryDefinitions extends Definer {

    public QueryDefinitions(Database database, String codeDirectory, String pkg) {
        super(database, codeDirectory, pkg);
    }

    public void generate() throws Throwable {
        defineTable("$$abc");
        defineTable("$$xyz", "x = {xValue}", 22);
        defineQueryForTable("SelectABCWhere", "$$abc", "SELECT * FROM $$abc WHERE a = {aVal}", 22);
        defineQuery("JoinABCXYZ", "SELECT * FROM $$abc, $$xyz WHERE x = a");
        defineQuery("JoinABCXYZWhere", "SELECT * FROM $$abc, $$xyz WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
        defineUpdate("ClearABC", "DELETE FROM $$abc");
        defineUpdate("ClearXYZ", "DELETE FROM $$xyz");
        defineUpdate("ClearABCWhere", "DELETE FROM $$abc WHERE a = {aValue}", 3);
        defineValueOf("ValueOfABCb", "SELECT b FROM $$abc");
        defineValueOf("ValueOfXYZz", "SELECT z FROM $$xyz WHERE x = {xValue}", 33);

        emitDatabaseAbstractionLayer("DatabaseAbstractionLayer");
    }
}
