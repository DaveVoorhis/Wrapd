package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.exceptions.ExceptionFatal;

import java.sql.SQLException;

/**
 * A Query definition.
 */
public class QueryDefinition {
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    public QueryDefinition(String queryName, String sqlText, Object... args) {
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
    }

    /**
     * Generate code to represent this QueryDefinition.
     *
     * @param database Database
     * @param codeDirectory Directory for generated code.
     * @return boolean true if successful, false if not.
     * @throws SQLException exception if DBMS access failed
     */
    // TODO consider returning Result rather than boolean
    public boolean generate(Database database, String codeDirectory) throws SQLException {
        var tupleClassName = queryName + "Tuple";
        var tupleClassCreated = (args == null || args.length == 0)
            ? database.createTupleFromQueryAll(codeDirectory, tupleClassName, sqlText)
            : database.createTupleFromQuery(codeDirectory, tupleClassName, sqlText, args);
        if (tupleClassCreated.isOk()) {
            var queryGenerator = new QueryTypeGenerator(codeDirectory, queryName, sqlText, args);
            var results = queryGenerator.compile();
            if (!results.compiled)
                throw new ExceptionFatal("Unable to generate Query derivative " + queryName + ": " + results.compilerMessages);
        }
        return true;
    }

}
