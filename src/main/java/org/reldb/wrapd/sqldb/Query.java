package org.reldb.wrapd.sqldb;

/**
 * An SQL SELECT (or equivalent RecordSet-returning) Query.
 *
 * Normally not used directly, but inherited.
 */
public class Query<T extends Tuple> extends SQL {
    private final Class<T> tupleClass;

    /**
     * Define a Query.
     *
     * @param queryText SQL text.
     * @param tupleClass Tuple class.
     * @param arguments Arguments to query.
     */
    protected Query(String queryText, Class<T> tupleClass, Object... arguments) {
        super(queryText, arguments);
        this.tupleClass = tupleClass;
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
     * Get Tuple class.
     *
     * @return Class&lt;T extends Tuple&gt;
     */
    public Class<T> getTupleClass() {
        return tupleClass;
    }
}
