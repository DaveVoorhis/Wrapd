package org.reldb.wrapd.sqldb;

import java.lang.reflect.Method;

/**
 * Mechanisms for defining Query classes.
 */
public class QueryDefiner {
    private final Database database;
    private final String codeDirectory;

    public static class QueryDefinerException extends Exception {
        private final QueryDefiner definer;
        private final Method method;

        public QueryDefinerException(QueryDefiner definer, Method method, Exception exception) {
            super(exception);
            this.definer = definer;
            this.method = method;
        }

        public QueryDefiner getQueryDefiner() {
            return definer;
        }

        public Method getMethod() {
            return method;
        }
    }

    /**
     * Create a QueryDefiner, given a Database and the directory where Tuple-drived classes will be stored.
     *
     * @param database Database
     * @param codeDirectory Directory for Tuple-derived classes.
     */
    public QueryDefiner(Database database, String codeDirectory) {
        this.database = database;
        this.codeDirectory = codeDirectory;
    }

    public Database getDatabase() {
        return database;
    }

    public String getCodeDirectory() {
        return codeDirectory;
    }

    /**
     * Run the QueryDefinition against the specified Database (see @QueryDefiner's constructor)
     * to create Query subclass that can be passed to a Database for future evaluation.
     *
     * @param definition QueryDefinition
     */
    protected boolean generate(QueryDefinition definition) throws Exception {
        return definition.generate(getDatabase(), getCodeDirectory());
    }

    /**
     * Shorthand for invoking generate(new QueryDefinition(...)).
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
