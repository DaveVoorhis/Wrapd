package org.reldb.wrapd.sqldb;

public class QueryDefinition {
    private String queryName;
    private String sqlText;
    private Object[] args;

    // TODO User-defined subclass of QueryDefinition gets run against the database to
    //  produce a Tuple subclass and generate a subclass of Query (named per queryName),
    //  all as part of a test/generate phase. The subclasses of Query are passed
    //  to Database instance for use in applications.

    public QueryDefinition(String queryName, String sqlText, Object... args) {
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
    }
}
