package org.reldb.wrapd.sqldb;

import java.sql.SQLException;

/**
 * An Update query definition, which normally maps to an UPDATE, INSERT or DELETE query or equivalent stored procedure invocation.
 */
public class UpdateDefinition {
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    /**
     * Create a Update definition.
     *
     * @param queryName Query name. Should be unique.
     * @param sqlText SQL query text.
     * @param args Query arguments. These may be any arguments that allow the (parametric) query to
     *             run and are of the same type as will be used in production.
     */
    public UpdateDefinition(String queryName, String sqlText, Object... args) {
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
    }

    /**
     * Generate code to represent this UpdateDefinition.
     *
     * @param database Database.
     * @param codeDirectory Directory for generated code.
     * @param packageSpec The package, in dotted notation, to which the generated class definition belongs.
     * @throws SQLException exception if DBMS access failed
     */
    public void generate(Database database, String codeDirectory, String packageSpec) throws SQLException {
        var updateGenerator = new UpdateTypeGenerator(codeDirectory, packageSpec, queryName, sqlText, args);
        updateGenerator.generate();
    }

}
