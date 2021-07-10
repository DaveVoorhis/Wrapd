package org.reldb.wrapd.schema;

import org.reldb.wrapd.exceptions.ExceptionFatal;

public abstract class AbstractSchema {

    protected interface Version {}

    protected class NewDatabase implements Version {}

    protected class Indeterminate implements Version {
        private String reason;
        public Indeterminate(String reason) {
            this.reason = reason;
        }
        String get() {
            return reason;
        }
    }

    protected class Number implements Version {
        private int number;
        public Number(int number) {
            this.number = number;
        }
        int get() {
            return number;
        }
    }

    /**
     * Return NewDatabase instance for new database.
     * Return Indeterminate instance for database where version can't be determined.
     * Return Number instance for database version.
     */
    public abstract Version getVersion();

    /**
     * Set version number.
     *
     * @param number
     */
    protected abstract boolean setVersion(Number number);

    protected abstract boolean createDatabase();

    public interface Update {
        boolean apply(AbstractSchema schema);
    }

    protected abstract Update[] getUpdates();

    public boolean establish() {
        var version = getVersion();
        if (version instanceof NewDatabase)
            return createDatabase();
        else {
            if (version instanceof Indeterminate)
                throw new ExceptionFatal("Unable to determine version due to: " + ((Indeterminate) version).get());
            if (!(version instanceof Number))
                throw new ExceptionFatal("Unrecognised version type: " + version.getClass().getName());
            int versionNumber = ((Number)version).get();
            var updates = getUpdates();
            for (int update = versionNumber + 1; update <= updates.length; update++) {
                if (!updates[update - 1].apply(this))
                    return false;
                if (!setVersion(new Number(update)))
                    return false;
            }
            return true;
        }
    }

}
