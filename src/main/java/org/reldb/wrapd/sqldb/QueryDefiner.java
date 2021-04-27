package org.reldb.wrapd.sqldb;

import java.lang.reflect.Method;
import java.sql.SQLException;

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
     * @param definition
     */
    protected boolean generate(QueryDefinition definition) throws Exception {
        return definition.generate(getDatabase(), getCodeDirectory());
    }

    /**
     * Scan this class for methods with name starting with 'query' (e.g., queryCustomers01) that
     * return a QueryDefinition.
     *
     * For each, run the QueryDefinition against the specified Database (see @QueryDefiner's constructor)
     * to create Query subclass that can be passed to a Database for future evaluation.
     *
     * @throws QueryDefinerException
     */
    public void generate() throws QueryDefinerException {
        for (Method method: getClass().getMethods()) {
            if (method.getName().toLowerCase().startsWith("query")
                    && method.getParameterCount() == 0
                    && method.getReturnType().equals(QueryDefinition.class)) {
                try {
                    generate((QueryDefinition)method.invoke(this));
                } catch (Exception e) {
                    throw new QueryDefinerException(this, method, e);
                }
            }
        }
    }
}
