package org.reldb.wrapd.tuples;

/**
 * Tuple attribute definition.
 */
public class Attribute {
    /** Attribute name. */
    public final String name;

    /** Attribute type. */
    public final Class<?> type;

    /**
     * Create an attribute.
     *
     * @param name Attribute name.
     * @param type Attribute type.
     */
    public Attribute(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }
}