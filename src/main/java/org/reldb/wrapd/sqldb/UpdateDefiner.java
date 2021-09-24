package org.reldb.wrapd.sqldb;

import java.lang.reflect.Method;

/**
 * Mechanisms for defining Update classes.
 */
public class UpdateDefiner {
    private final Database database;
    private final String codeDirectory;
    private final String packageSpec;

    /**
     * A UpdateeDefiner exception.
     */
    public static class UpdateDefinerException extends Exception {
        /** The UpdateDefiner in which the exception occurred. */
        public final UpdateDefiner definer;

        /** The defining Method that caused the exception. */
        public final Method method;

        /**
         * Create a QueryDefinerException.
         *
         * @param definer The QueryDefiner in which the exception occurred.
         * @param method The Method in which the exception occurred.
         * @param exception The underlying error thrown.
         */
        public UpdateDefinerException(UpdateDefiner definer, Method method, Exception exception) {
            super(exception);
            this.definer = definer;
            this.method = method;
        }
    }

    /**
     * Create a UpdateDefiner, given a Database, the directory where generated class definitions will be stored, and their package.
     *
     * @param database Database
     * @param codeDirectory Directory for generated class definitions.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     */
    public UpdateDefiner(Database database, String codeDirectory, String packageSpec) {
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
     * Run the UpdateDefinition against the specified Database (see @UpdateDefiner's constructor)
     * to create Update subclass that can be passed to a Database for future evaluation.
     *
     * @param definition UpdateDefinition.
     * @throws Exception Failed.
     */
    protected void generate(UpdateDefinition definition) throws Exception {
        definition.generate(getCodeDirectory(), getPackageSpec());
    }

    /**
     * Shorthand for invoking generate(new UpdateDefinition(...)).
     *
     * @param queryName Name of update query. Should be unique.
     * @param sqlText SQL update query text.
     * @param args Arguments that specify parameter type(s) and allow update query to succeed.
     * @throws Exception Error.
     */
    protected void define(String queryName, String sqlText, Object... args) throws Exception {
         generate(new UpdateDefinition(queryName, sqlText, args));
    }

    /**
     * Scan this class for methods with signature:
     * UpdateDefinition query*() (e.g., updateCustomers01)
     *   - or -
     * void update*() (which is assumed to invoke define(...))
     *
     * For each, run the UpdateDefinition against the specified Database (see @UpdateDefiner's constructor)
     * to create Update subclass that can be passed to a Database for future evaluation.
     *
     * @throws UpdateDefinerException if error
     */
    public void generate() throws UpdateDefinerException {
        for (var method: getClass().getMethods()) {
            if (method.getName().toLowerCase().startsWith("update") && method.getParameterCount() == 0) {
                if (method.getReturnType().equals(UpdateDefinition.class)) {
                    try {
                        generate((UpdateDefinition) method.invoke(this));
                    } catch (Exception e) {
                        throw new UpdateDefinerException(this, method, e);
                    }
                } else if (method.getReturnType().equals(Void.TYPE)) {
                    try {
                        method.invoke(this);
                    } catch (Exception e) {
                        throw new UpdateDefinerException(this, method, e);
                    }
                }
            }
        }
    }
}
