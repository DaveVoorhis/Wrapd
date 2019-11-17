package org.reldb.wrapd.tuples.generated;

/* WARNING: Auto-generated code. DO NOT EDIT!!! */

import org.reldb.wrapd.tuples.Tuple;

/** TestSelect tuple class version 0 */
public class TestSelect implements Tuple {
	/** Version number */
	public static final long serialVersionUID = 0;
	/** Field */
	public java.lang.Integer x;
	/** Field */
	public java.lang.Integer y;
	/** Create string representation of this tuple. */
	public String toString() {
		return String.format("TestSelect {x = %s, y = %s}", this.x, this.y);
	}
}