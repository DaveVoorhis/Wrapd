package org.reldb.wrapd.sqldb;

public class QueryDefinition02 extends QueryDefinition {
    public QueryDefinition02() {
        super("Query02","SELECT * FROM $$tester WHERE x > ? AND x < ?");
    }
}
