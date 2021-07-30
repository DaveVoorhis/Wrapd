package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.exceptions.FatalException;

import java.sql.SQLException;

/**
 * A Query definition.
 */
public class QueryDefinition {
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    /**
     * Create a Query definition.
     *
     * @param queryName Query name. Should be unique.
     * @param sqlText SQL query text.
     * @param args Query arguments. These may be any arguments that allow the (parametric) query to
     *             run and are of the same type as will be used in production.
     */
    public QueryDefinition(String queryName, String sqlText, Object... args) {
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
    }

    /**
     * Generate code to represent this QueryDefinition.
     *
     * @param database Database.
     * @param codeDirectory Directory for generated code.
     * @throws SQLException exception if DBMS access failed
     */
    public void generate(Database database, String codeDirectory) throws SQLException {
        var tupleClassName = queryName + "Tuple";
        var tupleClassCreated = (args == null || args.length == 0)
            ? database.createTupleFromQueryAll(codeDirectory, tupleClassName, sqlText)
            : database.createTupleFromQuery(codeDirectory, tupleClassName, sqlText, args);
        if (tupleClassCreated.isOk()) {
            var queryGenerator = new QueryTypeGenerator(codeDirectory, queryName, sqlText, args);
            var results = queryGenerator.compile();
            if (!results.compiled)
                throw new FatalException("Unable to generate Query derivative " + queryName + ": " + results.compilerMessages);
        }
    }

}
