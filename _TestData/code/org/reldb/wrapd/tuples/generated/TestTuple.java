package org.reldb.wrapd.tuples.generated;

/* WARNING: Auto-generated code. DO NOT EDIT!!! */

import org.reldb.wrapd.tuples.Tuple;

/** TestTuple tuple class version 0 */
public class TestTuple implements Tuple {
	/** Version number */
	public static final long serialVersionUID = 0;
	/** Field */
	public java.lang.String Col1;
	/** Field */
	public java.lang.Integer Col2;
	/** Field */
	public java.lang.Boolean Col3;
	/** Field */
	public java.lang.Float Col5;
	/** Create string representation of this tuple. */
	public String toString() {
		return String.format("TestTuple {Col1 = %s, Col2 = %s, Col3 = %s, Col5 = %s}", this.Col1, this.Col2, this.Col3, this.Col5);
	}
}