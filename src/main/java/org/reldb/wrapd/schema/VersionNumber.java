package org.reldb.wrapd.schema;

/**
 * The database is this Version.
 */
public class VersionNumber implements Version {
    public final int value;

    /**
     * Create VersionNumber of a given value.
     *
     * @param value The version number.
     */
    public VersionNumber(int value) {
        this.value = value;
    }
}
