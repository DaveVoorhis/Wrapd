package org.reldb.wrapd.schema;

public class VersionNumber implements Version {
    public final int value;

    public VersionNumber(int value) {
        this.value = value;
    }
}
