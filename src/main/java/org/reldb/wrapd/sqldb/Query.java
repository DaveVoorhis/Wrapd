package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.tuples.Tuple;

public class Query<T extends Tuple> {
    private final String text;
    private final Class<T> tupleClass;
    private final Object[] arguments;

    // TODO probably should be protected, since eventually only subclasses of this should be exposed
    protected Query(String queryText, Class<T> tupleClass, Object... arguments) {
        this.text = queryText;
        this.tupleClass = tupleClass;
        this.arguments = arguments;
    }

    // TODO probably should be protected, since eventually only subclasses of this should be exposed
    protected Query(String queryText, Class<T> tupleClass) {
        this(queryText, tupleClass, new Object[] {});
    }

    public String getQueryText() {
        return text;
    }

    public Class<T> getTupleClass() {
        return tupleClass;
    }

    protected Object[] getArguments() {
        return arguments;
    }
}
