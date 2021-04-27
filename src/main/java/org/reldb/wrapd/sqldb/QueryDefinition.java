package org.reldb.wrapd.sqldb;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryDefinition {
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    public QueryDefinition(String queryName, String sqlText, Object... args) {
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
    }

    // TODO User-defined subclass of QueryDefinition gets run against the database to
    //  produce a Tuple subclass and generate a subclass of Query (named per queryName),
    //  all as part of a test/generate phase. The subclasses of Query are later passed
    //  to Database instance for use in applications.

    public boolean generate(Database database, String codeDirectory) throws SQLException {
        return (args == null || args.length == 0)
            ? database.createTupleFromQueryAll(codeDirectory, queryName, sqlText)
            : database.createTupleFromQuery(codeDirectory, queryName, sqlText, args);
    }

}
