package org.reldb.wrapd.sqldb;

/**
 * A SQL update (typically INSERT, UPDATE, DELETE, or stored procedure invoking) query. Normally not used directly, but inherited.
 */
public class Update {
    private final String text;
    private final Object[] arguments;

    /**
     * Define an update query.
     *
     * @param queryText SQL text.
     * @param arguments Arguments to query.
     */
    protected Update(String queryText, Object... arguments) {
        this.text = queryText;
        this.arguments = arguments;
    }

    /**
     * Define an update query.
     *
     * @param queryText SQL text.
     */
    protected Update(String queryText) {
        this(queryText, new Object[] {});
    }

    /**
     * Get query SQL text.
     *
     * @return SQL text.
     */
    public String getQueryText() {
        return text;
    }

    /**
     * Get array of arguments.
     *
     * @return Array of arguments.
     */
    protected Object[] getArguments() {
        return arguments;
    }
}
