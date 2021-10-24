package org.reldb.wrapd.sqldb;

/**
 * Abstract base class for (possibly parametric) SQL definitions.
 *
 * Not used directly; must be inherited.
 */
public abstract class SQL {
    private final String text;
    private final Object[] arguments;

    /**
     * Define a SQL construct.
     *
     * @param queryText SQL text.
     * @param arguments Arguments to query.
     */
    protected SQL(String queryText, Object... arguments) {
        this.text = queryText;
        this.arguments = arguments;
    }

    /**
     * Define a SQL construct.
     *
     * @param queryText SQL text.
     */
    protected SQL(String queryText) {
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
