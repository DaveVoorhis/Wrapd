package org.reldb.wrapd.schema;

import org.reldb.toolbox.strings.Str;
import org.reldb.toolbox.utilities.ProgressIndicator;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.response.Result;

import static org.reldb.wrapd.il8n.Strings.*;

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

    protected UpdateTransaction getTransaction() {
        return action -> action.run();
    }

    public Result setup(ProgressIndicator progressIndicator) {
        var version = getVersion();
        if (version instanceof NewDatabase)
            return createDatabase();
        else {
            if (version instanceof Indeterminate) {
                var noVersion = (Indeterminate)version;
                return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnableToDetermineVersion, noVersion.reason), noVersion.error));
            }
            if (!(version instanceof Number))
                return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnrecognisedVersionType, version.getClass().getName())));
            int versionNumber = ((Number)version).value;
            var updates = getUpdates();
            if (progressIndicator != null)
                progressIndicator.initialise(updates.length);
            for (int update = versionNumber + 1; update <= updates.length; update++) {
                var transaction = getTransaction();
                final int updateNumber = update;
                var result = transaction.run(() -> {
                    var updateResult = updates[updateNumber - 1].apply(this);
                    if (updateResult.isError())
                        return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnableToUpdateToVersion, updateNumber), updateResult.error));
                    var setVersionResult = setVersion(new Number(updateNumber));
                    if (setVersionResult.isError())
                        return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnableToSetVersion, updateNumber), setVersionResult.error));
                    return updateResult;
                });
                if (result.isError())
                    return result;
                if (progressIndicator != null)
                    progressIndicator.move(versionNumber, "Version " + versionNumber);
            }
            return Result.OK;
        }
    }

    public Result setup() {
        return setup(null);
    }

}
