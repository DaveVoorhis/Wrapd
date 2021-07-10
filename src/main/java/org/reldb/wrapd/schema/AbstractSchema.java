package org.reldb.wrapd.schema;

import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.response.Result;

public abstract class AbstractSchema {

    protected interface Version {}

    protected class NewDatabase implements Version {}

    protected class Indeterminate implements Version {
        private final String reason;
        private final Throwable error;
        public Indeterminate(String reason, Throwable error) {
            this.reason = reason;
            this.error = error;
        }
        public Indeterminate(String reason) {
            this(reason, null);
        }
        public Indeterminate(Throwable error) {
            this(null, error);
        }
    }

    protected class Number implements Version {
        public final int value;
        public Number(int value) {
            this.value = value;
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
    protected abstract Result setVersion(Number number);

    protected abstract Result createDatabase();

    public interface Update {
        Result apply(AbstractSchema schema);
    }

    protected abstract Update[] getUpdates();

    public Result establish() {
        var version = getVersion();
        if (version instanceof NewDatabase)
            return createDatabase();
        else {
            if (version instanceof Indeterminate)
                throw new ExceptionFatal("Unable to determine version due to: " + ((Indeterminate) version).get());
            if (!(version instanceof Number))
                throw new ExceptionFatal("Unrecognised version type: " + version.getClass().getName());
            int versionNumber = ((Number)version).value;
            var updates = getUpdates();
            for (int update = versionNumber + 1; update <= updates.length; update++) {
                var updateResult = updates[update - 1].apply(this);
                if (updateResult.isError())
                    return Result.ERROR(updateResult.error);
                var setVersionResult = setVersion(new Number(update));
                if (setVersionResult.isError())
                    return Result.ERROR(setVersionResult.error);
            }
            return Result.OK;
        }
    }

}
