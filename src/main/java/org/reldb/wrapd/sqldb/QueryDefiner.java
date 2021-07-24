package org.reldb.wrapd.sqldb;

import java.lang.reflect.Method;

/**
 * Mechanisms for defining Query classes.
 */
public class QueryDefiner {
    private final Database database;
    private final String codeDirectory;

    /**
     * A QueryDefiner exception.
     */
    public static class QueryDefinerException extends Exception {
        /** The QueryDefiner in which the exception occurred. */
        public final QueryDefiner definer;

        /** The defining Method that caused the exception. */
        public final Method method;

        /**
         * Create a QueryDefinerException.
         *
         * @param definer The QueryDefiner in which the exception occurred.
         * @param method The Method in which the exception occurred.
         * @param exception The underlying error thrown.
         */
        public QueryDefinerException(QueryDefiner definer, Method method, Exception exception) {
            super(exception);
            this.definer = definer;
            this.method = method;
        }
    }

    /**
     * Create a QueryDefiner, given a Database and the directory where Tuple-derived classes will be stored.
     *
     * @param database Database
     * @param codeDirectory Directory for Tuple-derived classes.
     */
    public QueryDefiner(Database database, String codeDirectory) {
        this.database = database;
        this.codeDirectory = codeDirectory;
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
     * Run the QueryDefinition against the specified Database (see @QueryDefiner's constructor)
     * to create Query subclass that can be passed to a Database for future evaluation.
     *
     * @param definition QueryDefinition.
     * @return True if generation is successful.
     * @throws Exception if failed.
     */
    protected boolean generate(QueryDefinition definition) throws Exception {
        return definition.generate(getDatabase(), getCodeDirectory());
    }

    /**
     * Shorthand for invoking generate(new QueryDefinition(...)).
     *
     * @param queryName Name of query. Should be unique.
     * @param sqlText SQL query text.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @throws Exception Error.
     */
    protected void define(String queryName, String sqlText, Object... args) throws Exception {
         generate(new QueryDefinition(queryName, sqlText, args));
    }

    /**
     * Scan this class for methods with signature:
     * QueryDefinition query*() (e.g., queryCustomers01)
     *   - or -
     * void query*() (which is assumed to invoke define(...))
     *
     * For each, run the QueryDefinition against the specified Database (see @QueryDefiner's constructor)
     * to create Query subclass that can be passed to a Database for future evaluation.
     *
     * @throws QueryDefinerException if error
     */
    public void generate() throws QueryDefinerException {
        for (Method method: getClass().getMethods()) {
            if (method.getName().toLowerCase().startsWith("query") && method.getParameterCount() == 0) {
                if (method.getReturnType().equals(QueryDefinition.class)) {
                    try {
                        generate((QueryDefinition) method.invoke(this));
                    } catch (Exception e) {
                        throw new QueryDefinerException(this, method, e);
                    }
                } else if (method.getReturnType().equals(Void.TYPE)) {
                    try {
                        method.invoke(this);
                    } catch (Exception e) {
                        throw new QueryDefinerException(this, method, e);
                    }
                }
            }
        }
    }
}
