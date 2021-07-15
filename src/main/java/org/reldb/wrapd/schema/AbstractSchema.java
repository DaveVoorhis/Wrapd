package org.reldb.wrapd.schema;

import org.reldb.toolbox.strings.Str;
import org.reldb.toolbox.utilities.EmptyProgressIndicator;
import org.reldb.toolbox.utilities.ProgressIndicator;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.response.Result;

import static org.reldb.wrapd.il8n.Strings.*;

public abstract class AbstractSchema {

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
    protected abstract Result setVersion(VersionNumber number);

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
        if (version == null)
            return Result.ERROR(new ExceptionFatal(Str.ing(ErrNullVersion)));
        if (version instanceof VersionIndeterminate) {
            var noVersion = (VersionIndeterminate)version;
            return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnableToDetermineVersion, noVersion.reason), noVersion.error));
        }
        final ProgressIndicator progress = (progressIndicator != null)
            ? progressIndicator
            : new EmptyProgressIndicator();
        var updates = getUpdates();
        int versionNumber = 0;
        if (version instanceof VersionNewDatabase) {
            progress.initialise(updates.length * 2 + 1);
            progress.move(0, "Creating schema");
            var createResult = createDatabase();
            if (createResult.isError()) {
                progress.move(0, "Creating schema failed");
                return createResult;
            }
            progress.move(1, "Schema created");
        } else {
            if (!(version instanceof VersionNumber))
                return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnrecognisedVersionType, version.getClass().getName())));
            versionNumber = ((VersionNumber)version).value;
            progress.initialise((updates.length - versionNumber) * 2);
        }
        var result = Result.OK;
        for (int update = versionNumber + 1; update <= updates.length && result.isOk(); update++) {
            var transaction = getTransaction();
            progress.move(progress.getValue() + 1, "Updating to version " + update);
            final int updateNumber = update;
            result = transaction.run(() -> {
                var updateResult = updates[updateNumber - 1].apply(this);
                if (updateResult.isError()) {
                    progress.move(progress.getValue(), "Failed to update to version " + updateNumber);
                    return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnableToUpdateToVersion, updateNumber), updateResult.error));
                }
                var setVersionResult = setVersion(new VersionNumber(updateNumber));
                if (setVersionResult.isError()) {
                    progress.move(progress.getValue(), "Failed to record update to version " + updateNumber);
                    return Result.ERROR(new ExceptionFatal(Str.ing(ErrUnableToSetVersion, updateNumber), setVersionResult.error));
                }
                return updateResult;
            });
            progress.move(progress.getValue() + 1, "Updated to version " + update);
        }
        return result;
    }

    public Result setup() {
        return setup(null);
    }

}
