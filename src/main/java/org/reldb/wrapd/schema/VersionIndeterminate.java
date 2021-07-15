package org.reldb.wrapd.schema;

public class VersionIndeterminate implements Version {
    public final String reason;
    public final Throwable error;

    public VersionIndeterminate(String reason, Throwable error) {
        this.reason = reason;
        this.error = error;
    }

    public VersionIndeterminate(String reason) {
        this(reason, null);
    }

    public VersionIndeterminate(Throwable error) {
        this(null, error);
    }
}
