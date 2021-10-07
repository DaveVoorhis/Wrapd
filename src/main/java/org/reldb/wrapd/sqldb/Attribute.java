package org.reldb.wrapd.sqldb;

/**
 * Represents an attribute of a Tuplee.
 */
public class Attribute {
    /** Name of attribute. */
    final String name;

    /** Type of attribute. */
    final Class<?> type;

    /**
     * Constructor.
     *
     * @param name Name of attribute.
     * @param type Type of attribute.
     */
    Attribute(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }
}
