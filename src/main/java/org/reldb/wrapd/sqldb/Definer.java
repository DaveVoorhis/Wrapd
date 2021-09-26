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
     * Return the Database.
     *
     * @return Database.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Return directory where code is stored.
     *
     * @return Code directory path.
     */
    public String getCodeDirectory() {
        return codeDirectory;
    }

    /**
     * Return the package for generated code.
     *
     * @return Package specification for generated code.
     */
    public String getPackageSpec() {
        return packageSpec;
    }

    /**
     * Define a Query for future use.
     *
     * @param queryName Name of query. Should be unique.
     * @param sqlText SQL query text.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @throws Throwable Error.
     */
    protected void defineQuery(String queryName, String sqlText, Object... args) throws Throwable {
        var tupleClassName = queryName + "Tuple";
        var tupleClassCreated = (args == null || args.length == 0)
                ? database.createTupleFromQueryAll(codeDirectory, packageSpec, tupleClassName, sqlText)
                : database.createTupleFromQuery(codeDirectory, packageSpec, tupleClassName, sqlText, args);
        if (tupleClassCreated.isOk()) {
            var queryGenerator = new QueryTypeGenerator(codeDirectory, packageSpec, queryName, sqlText, args);
            queryGenerator.generate();
        } else
            throw tupleClassCreated.error;
    }

    /**
     * Define an Update for future use.
     *
     * @param queryName Name of update query. Should be unique.
     * @param sqlText SQL update query text.
     * @param args Arguments that specify parameter type(s) and allow update query to succeed.
     * @throws Throwable Error.
     */
    protected void defineUpdate(String queryName, String sqlText, Object... args) throws Throwable {
        // Test the query by running it; hope you're not doing this on a production database!
        if (args == null || args.length == 0)
            database.updateAll(sqlText);
        else
            database.update(sqlText, args);
        var updateGenerator = new UpdateTypeGenerator(codeDirectory, packageSpec, queryName, sqlText, args);
        updateGenerator.generate();
    }

    /**
     * Define a Query for future use, with a Tuple type that has an insert(...) and an update(...)
     * for the specified table. The Query is SELECT * FROM tableName.
     *
     * @param tableName Name of the table, optionally including $$.
     * @throws Throwable Error.
     */
    protected void defineTable(String tableName) throws Throwable {
        var queryName = tableName.replaceAll("\\$\\$", "");
        var tupleClassName = queryName + "Tuple";
        var tableInfo = new Database.TableInfo(tableName);
        var tupleClassCreated = database.createTupleFromTable(codeDirectory, packageSpec, tupleClassName, tableInfo);
        if (tupleClassCreated.isOk()) {
            var queryGenerator = new QueryTypeGenerator(codeDirectory, packageSpec, queryName, tableInfo.getSQLText(), null);
            queryGenerator.generate();
        } else
            throw tupleClassCreated.error;
    }

}
