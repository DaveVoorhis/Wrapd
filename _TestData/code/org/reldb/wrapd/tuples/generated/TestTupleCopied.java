package org.reldb.wrapd.tuples.generated;

/* WARNING: Auto-generated code. DO NOT EDIT!!! */

import org.reldb.wrapd.tuples.Tuple;

/** TestTupleCopied tuple class version 1 */
public class TestTupleCopied implements Tuple {
	/** Version number */
	public static final long serialVersionUID = 1;
	/** Field */
	public java.lang.String Col1;
	/** Field */
	public java.lang.Integer Col2;
	/** Field */
	public java.lang.Float Col5;
	/** Field */
	public java.lang.Integer Col6;
	/** Method to copy from specified tuple to this tuple.
	@param source - tuple to copy from */
	public void copyFrom(TestTupleRenamed source) {
		this.Col1 = source.Col1;
		this.Col2 = source.Col2;
		this.Col5 = source.Col5;
	}
	/** Create string representation of this tuple. */
	public String toString() {
		return String.format("TestTupleCopied {Col1 = %s, Col2 = %s, Col5 = %s, Col6 = %s}", this.Col1, this.Col2, this.Col5, this.Col6);
	}
}