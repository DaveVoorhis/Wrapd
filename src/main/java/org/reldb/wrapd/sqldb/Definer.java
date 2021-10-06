package org.reldb.wrapd.sqldb;

/**
 * Mechanism for defining Query and Update classes.
 */
public class Definer {
    private final Database database;
    private final String codeDirectory;
    private final String packageSpec;

    /**
     * Create a Definer, given a Database, the directory where generated class definitions will be stored, and their package.
     *
     * @param database Database
     * @param codeDirectory Directory for generated class definitions.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     */
    public Definer(Database database, String codeDirectory, String packageSpec) {
        this.database = database;
        this.codeDirectory = codeDirectory;
        this.packageSpec = packageSpec;
    }

    /**
     * Define a Query for future use.
     *
     * @param queryName Name of query. Should be unique.
     * @param tableName Name of table the generated Tuple maps to. Null if not mapped to a table.
     *                  If not null, the Tuple type will have an insert(...) and an update(...)
     *                  for the specified table.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @throws Throwable Error.
     */
    public void defineQueryForTable(String queryName, String tableName, String sqlText, Object... args) throws Throwable {
        var tupleClassName = queryName + "Tuple";
        var queryGenerator = new QueryTypeGenerator(codeDirectory, packageSpec, tupleClassName, queryName, sqlText, args);
        queryGenerator.setTableName(tableName);
        queryGenerator.generate();
        var regeneratedSqlText = queryGenerator.getSQLText();
        var tupleClassCreated = (args == null || args.length == 0)
                ? database.createTupleFromQueryAllForUpdate(codeDirectory, packageSpec, tupleClassName, tableName, regeneratedSqlText)
                : database.createTupleFromQueryForUpdate(codeDirectory, packageSpec, tupleClassName, tableName, regeneratedSqlText, args);
        if (tupleClassCreated.isError())
            //noinspection ConstantConditions
            throw tupleClassCreated.error;
    }

    /**
     * Define a Query for future use.
     *
     * @param queryName Name of query. Should be unique.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @throws Throwable Error.
     */
    public void defineQuery(String queryName, String sqlText, Object... args) throws Throwable {
        defineQueryForTable(queryName, null, sqlText, args);
    }

    /**
     * Define an Update for future use.
     *
     * WARNING: This will test the query by running it. Hope you're not doing this on a production database!
     *
     * @param queryName Name of update query. Should be unique.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow update query to succeed.
     * @throws Throwable Error.
     */
    public void defineUpdate(String queryName, String sqlText, Object... args) throws Throwable {
        var updateGenerator = new UpdateTypeGenerator(codeDirectory, packageSpec, queryName, sqlText, args);
        updateGenerator.generate();
        if (args == null || args.length == 0)
            database.updateAll(updateGenerator.getSQLText());
        else
            database.update(updateGenerator.getSQLText(), args);
    }

    /**
     * Define a Query for future use, with a Tuple type that has an insert(...) and an update(...)
     * for the specified table. The Query is SELECT * FROM tableName WHERE whereClause.
     *
     * @param tableName Name of the table, optionally including $$.
     * @param whereClause The WHERE clause without the 'WHERE' keyword.
     *                    Parameters may be specified as ? or {name}. If {name} is used, it will
     *                    appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                    is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                    SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @throws Throwable Error.
     */
    public void defineTable(String tableName, String whereClause, Object... args) throws Throwable {
        var queryName = tableName.replaceAll("\\$\\$", "");
        var realTableName = database.replaceTableNames(tableName);
        var query = "SELECT * FROM " + realTableName +
                (whereClause != null && !whereClause.isEmpty()
                        ? " WHERE " + whereClause
                        : "");
        defineQueryForTable(queryName, realTableName, query, args);
    }

    /**
     * Define a Query for future use, with a Tuple type that has an insert(...) and an update(...)
     * for the specified table. The Query is SELECT * FROM tableName.
     *
     * @param tableName Name of the table, optionally including $$.
     * @throws Throwable Error.
     */
    public void defineTable(String tableName) throws Throwable {
        defineTable(tableName, null, (Object[])null);
    }

    /** Define a Query for future use that returns the first column of the first row. */
    public void defineValueOf(String query, Object... args) throws Throwable {
        // TODO - implement this.
    }
}
