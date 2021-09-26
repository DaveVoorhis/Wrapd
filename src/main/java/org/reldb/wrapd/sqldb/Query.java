package org.reldb.wrapd.sqldb;

/**
 * A SQL SELECT (or equivalent RecordSet-returning) Query. Normally not used directly, but inherited.
 */
public class Query<T extends Tuple> {
    private final String text;
    private final Class<T> tupleClass;
    private final Object[] arguments;

    /**
     * Define a Query.
     *
     * @param queryText SQL text.
     * @param tupleClass Tuple class.
     * @param arguments Arguments to query.
     */
    protected Query(String queryText, Class<T> tupleClass, Object... arguments) {
        this.text = queryText;
        this.tupleClass = tupleClass;
        this.arguments = arguments;
    }

    /**
     * Define a Query.
     *
     * @param queryText SQL text.
     * @param tupleClass Tuple class.
     */
    protected Query(String queryText, Class<T> tupleClass) {
        this(queryText, tupleClass, new Object[] {});
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
     * Get Tuple class.
     *
     * @return Class&lt;T extends Tuple&gt;
     */
    public Class<T> getTupleClass() {
        return tupleClass;
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
