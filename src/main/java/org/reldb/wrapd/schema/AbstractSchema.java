package org.reldb.wrapd.schema;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.progress.EmptyProgressIndicator;
import org.reldb.toolbox.progress.ProgressIndicator;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.response.Result;

/**
 * An abstract schema definition that can handle updates on anything definable as a schema.
 */
public abstract class AbstractSchema {
    private static final Msg ErrUnableToDetermineVersion = new Msg("Unable to determine version due to: {0}", AbstractSchema.class);
    private static final Msg ErrUnrecognisedVersionType = new Msg("Unrecognised version type: {0}", AbstractSchema.class);
    private static final Msg ErrUnableToUpdateToVersion = new Msg("Unable to perform update to version {0}", AbstractSchema.class);
    private static final Msg ErrUnableToSetVersion = new Msg("Unable to set version number to {0}", AbstractSchema.class);
    private static final Msg ErrNullVersion = new Msg("Version is null.", AbstractSchema.class);
    private static final Msg ErrFailedToRecordUpdateToVersion = new Msg("Failed to record update to version {0}", AbstractSchema.class);
    private static final Msg ErrFailedToUpdateToVersionDueToException = new Msg("Failed to update to version {0} due to exception", AbstractSchema.class);
    private static final Msg ErrFailedToUpdateToVersionDueToFalse = new Msg("Failed to update to version {0} due to false result", AbstractSchema.class);

    private static final Msg MsgCreatingSchema = new Msg("Creating schema");
    private static final Msg MsgCreatingSchemaFailed = new Msg("Creating schema failed");
    private static final Msg MsgSchemaCreated = new Msg("Schema created");
    private static final Msg MsgUpdatingToVersion = new Msg("Updating to version {0}");
    private static final Msg MsgUpdatedToVersion = new Msg("Updated to version {0}");

    /**
     * Return VersionNewDatabase instance for new database.
     * Return VersionIndeterminate instance for database where version can't be determined.
     * Return VersionNumber instance for database version.
     *
     * @return The Version.
     */
    public abstract Version getVersion();

    /**
     * Set new version number after successful update. Normally not called directly;
     * only by setup(...)
     *
     * @param number - version number
     * @return Result
     */
    protected abstract Result setVersion(VersionNumber number);

    /**
     * Create and initialise a new schema. Typically only contains version store,
     * because it is assumed that updates will be run to create other content.
     *
     * @return Result
     */
    protected abstract Result create();

    /**
     * Definition of a schema update.
     */
    public interface Update {
        /**
         * Apply an schema update.
         *
         * @param schema The schema to which the update applies.
         * @return Result of application of update.
         * @throws Throwable Thrown if failure.
         */
        Result apply(AbstractSchema schema) throws Throwable;
    }

    /**
     * A collection of schema updates.
     *
     * Only add new updates to the end.
     *
     * @return array of UpdateS.
     */
    protected abstract Update[] getUpdates();

    /**
     * Obtain a new transaction. Within setup(...), each update obtained from @getUpdates() will
     * run, followed by updating version via setVersion(...) in its own UpdateTransaction.
     *
     * @return UpdateTransaction.
     */
    protected UpdateTransaction getTransaction() {
        return ResultAction::run;
    }

    /**
     * Verify schema exists and is up-to-date. Otherwise, create it and/or apply updates if necessary.
     *
     * @param progressIndicator ProgressIndicator
     * @return Result
     */
    public Result setup(ProgressIndicator progressIndicator) {
        var version = getVersion();
        if (version == null)
            return Result.ERROR(new FatalException(Str.ing(ErrNullVersion)));
        if (version instanceof VersionIndeterminate) {
            var noVersion = (VersionIndeterminate)version;
            return Result.ERROR(new FatalException(Str.ing(ErrUnableToDetermineVersion, noVersion.reason), noVersion.error));
        }
        final ProgressIndicator progress = (progressIndicator != null)
            ? progressIndicator
            : new EmptyProgressIndicator();
        var updates = getUpdates();
        int versionNumber = 0;
        if (version instanceof VersionNewDatabase) {
            progress.initialise(updates.length + 1);
            progress.move(0, Str.ing(MsgCreatingSchema));
            var createResult = create();
            if (createResult.isError()) {
                progress.move(progress.getValue(), Str.ing(MsgCreatingSchemaFailed));
                return createResult;
            }
            var setVersionResult = setVersion(new VersionNumber(0));
            if (setVersionResult.isError()) {
                progress.move(progress.getValue(), Str.ing(ErrFailedToRecordUpdateToVersion, 0));
                return Result.ERROR(new FatalException(Str.ing(ErrUnableToSetVersion, 0), setVersionResult.error));
            }
            progress.move(progress.getValue() + 1, Str.ing(MsgSchemaCreated));
        } else {
            if (!(version instanceof VersionNumber))
                return Result.ERROR(new FatalException(Str.ing(ErrUnrecognisedVersionType, version.getClass().getName())));
            versionNumber = ((VersionNumber)version).value;
            progress.initialise(updates.length - versionNumber);
        }
        var result = Result.OK;
        for (int update = versionNumber + 1; update <= updates.length && result.isOk(); update++) {
            var transaction = getTransaction();
            progress.move(progress.getValue(), Str.ing(MsgUpdatingToVersion, update));
            final int updateNumber = update;
            result = transaction.run(() -> {
                var updateSpecification = updates[updateNumber - 1];
                Result updateResult;
                try {
                    updateResult = updateSpecification.apply(this);
                } catch (Throwable t) {
                    progress.move(progress.getValue(), Str.ing(ErrFailedToUpdateToVersionDueToException, updateNumber));
                    return Result.ERROR(new FatalException(Str.ing(ErrUnableToUpdateToVersion, updateNumber), t));
                }
                if (updateResult.isError()) {
                    progress.move(progress.getValue(), Str.ing(ErrFailedToUpdateToVersionDueToFalse, updateNumber));
                    return Result.ERROR(new FatalException(Str.ing(ErrUnableToUpdateToVersion, updateNumber), updateResult.error));
                }
                var setVersionResult = setVersion(new VersionNumber(updateNumber));
                if (setVersionResult.isError()) {
                    progress.move(progress.getValue(), Str.ing(ErrFailedToRecordUpdateToVersion, updateNumber));
                    return Result.ERROR(new FatalException(Str.ing(ErrUnableToSetVersion, updateNumber), setVersionResult.error));
                }
                return updateResult;
            });
            if (result.isOk())
                progress.move(progress.getValue() + 1, Str.ing(MsgUpdatedToVersion, update));
        }
        return result;
    }

    /**
     * Verify schema exists and is up-to-date. Otherwise, create it and/or apply updates if necessary.
     *
     * @return Result
     */
    public Result setup() {
        return setup(null);
    }

}
