package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.tuples.Tuple;

@FunctionalInterface
public interface TupleProcessor {
	public void process(Tuple tupleType);
}