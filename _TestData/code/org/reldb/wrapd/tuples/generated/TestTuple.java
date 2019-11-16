package org.reldb.wrapd.tuples.generated;

import org.reldb.wrapd.tuples.Tuple;

/** org.reldb.wrapd.tuples.generated.TestTuple tuple class version 0 */
public class org.reldb.wrapd.tuples.generated.TestTuple implements Tuple {
	/** Version number */
	public static final long serialVersionUID = 0;
	/** Field */
	public java.lang.String Col1;
	/** Field */
	public java.lang.Integer Col2;
	/** Field */
	public java.lang.Boolean Col3;
	/** Field */
	public java.lang.Double Col4;
	/** Create string representation of this tuple. */
	public String toString() {
		return String.format("org.reldb.wrapd.tuples.generated.TestTuple {Col1 = %s, Col2 = %s, Col3 = %s, Col4 = %s}", this.Col1, this.Col2, this.Col3, this.Col4);
	}
}