package org.reldb.wrapd.sqldb;

public class QueryDefinition01 extends QueryDefinition {
    public QueryDefinition01() {
        super("Query01","SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
    }
}
