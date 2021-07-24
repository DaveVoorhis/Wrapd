package org.reldb.wrapd.schema;

/**
 * An indeterminate Version. We don't know the version, but we know why we don't know the version.
 */
public class VersionIndeterminate implements Version {
    /** Reason for indeterminacy. */
    public final String reason;

    /** Error that caused indeterminacy. */
    public final Throwable error;

    /**
     * Create a VersionIndeterminate, given the reason it's indeterminate and the error that caused it to be indeterminate.
     *
     * @param reason Reason for indeterminacy.
     * @param error Error that caused indeterminacy.
     */
    public VersionIndeterminate(String reason, Throwable error) {
        this.reason = reason;
        this.error = error;
    }

    /**
     * Create a VersionIndeterminate, given the reason it's indeterminate.
     *
     * @param reason Reason for indeterminacy.
     */
    public VersionIndeterminate(String reason) {
        this(reason, null);
    }

    /**
     * Create a VersionIndeterminate, given the error that caused it to be indeterminate.
     *
     * @param error Throwable that caused indeterminacy.
     */
    public VersionIndeterminate(Throwable error) {
        this(null, error);
    }
}
