package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.tuples.Tuple;

public class Query<T extends Tuple> {
    private final String text;
    private final Class<T> tupleClass;

    public Query(String queryText, Class<T> tupleClass) {
        this.text = queryText;
        this.tupleClass = tupleClass;
    }

    public String getQueryText() {
        return text;
    }

    public Class<T> getTupleClass() {
        return tupleClass;
    }
}
