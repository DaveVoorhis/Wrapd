package org.reldb.wrapd.schema;

import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database;

import java.sql.SQLException;
import java.util.Optional;

import static org.reldb.wrapd.il8n.Strings.ErrVersionTableIsEmpty;
import static org.reldb.wrapd.il8n.Strings.ErrVersionTableValueIsInvalid;

/**
 * A SQL schema migrator.
 */
public abstract class SQLSchema extends AbstractSchema {
    private final Database database;

    private String versionTableName = "$$__version";
    private String versionTableAttributeName = "version";
    private String versionTableAttributeTypeName = "integer";

    /**
     * Create an instance of a schema for a specified Database.
     *
     * @param database Database.
     */
    public SQLSchema(Database database) {
        this.database = database;
    }

    /**
     * Obtain the database.
     *
     * @return Database.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Set the name of the table that contains the version number.
     *
     * @param versionTableName The table name.
     */
    public void setVersionTableName(String versionTableName) {
        this.versionTableName = versionTableName;
    }

    /**
     * Get the name of the table that contains the schema version number.
     *
     * @return The name of the table that contains the schema version number.
     */
    public String getVersionTableName() {
        return versionTableName;
    }

    /**
     * Set the name of the column of the version table that contains the version number.
     *
     * @param versionTableAttributeName The column name.
     */
    public void setVersionTableAttributeName(String versionTableAttributeName) {
        this.versionTableAttributeName = versionTableAttributeName;
    }

    /**
     * Get the name of the column of the version table that contains the version number.
     *
     * @return The name of the column of the version table that contains the version number.
     */
    public String getVersionTableAttributeName() {
        return versionTableAttributeName;
    }

    /**
     * Set the name of the type of the column of the version table that contains the version number.
     *
     * @param versionTableAttributeTypeName The type name.
     */
    public void setVersionTableAttributeTypeName(String versionTableAttributeTypeName) {
        this.versionTableAttributeTypeName = versionTableAttributeTypeName;
    }

    /**
     * Get the name of the type of the column of the version table that contains the version number.
     *
     * @return Name of the type of the column of the version table that contains the version number.
     */
    public String getVersionTableAttributeTypeName() {return versionTableAttributeTypeName;}

    @Override
    public Version getVersion() {
        Optional<?> versionRaw;
        try {
            versionRaw = database.valueOfAll("SELECT " + getVersionTableAttributeName() + " FROM " + getVersionTableName(), getVersionTableAttributeName());
        } catch (SQLException sqe) {
            // TODO improve!
            // This (rather questionably) assumes that if attempting to get the version fails with a SQLException,
            // it must be because the database is new. It might not be. But alternatives, like
            // using database metadata to determine whether the table exists or not, are fraught
            // with case-sensitivity perils that vary from DBMS to DBMS.
            return new VersionNewDatabase();
        }
        if (versionRaw.isEmpty())
            return new VersionIndeterminate(Str.ing(ErrVersionTableIsEmpty, getVersionTableName()));
        var versionStr = versionRaw.get().toString();
        int version;
        try {
            version = Integer.parseInt(versionStr);
        } catch (NumberFormatException nfe) {
            return new VersionIndeterminate(Str.ing(ErrVersionTableValueIsInvalid, getVersionTableName(), getVersionTableAttributeName()));
        }
        return new VersionNumber(version);
    }

    @Override
    protected UpdateTransaction getTransaction() {
        return action -> {
            try {
                return database.processTransaction(transaction -> action.run());
            } catch (SQLException sqe) {
                return Result.ERROR(sqe);
            }
        };
    }

    @Override
    protected Result setVersion(VersionNumber number) {
        try {
            database.update("UPDATE " + getVersionTableName() + " SET " + getVersionTableAttributeName() + " = ?", number.value);
            return Result.OK;
        } catch (SQLException sqe) {
            return Result.ERROR(sqe);
        }
    }

    @Override
    protected Result create() {
        try {
            return database.transact(xact -> {
                xact.updateAll("CREATE TABLE " + getVersionTableName() + "(" + getVersionTableAttributeName() + " " + getVersionTableAttributeTypeName() + ")");
                xact.updateAll("INSERT INTO " + getVersionTableName() + "(" + getVersionTableAttributeName() + ") VALUES (0)");
                return Result.OK;
            });
        } catch (SQLException sqe) {
            return Result.ERROR(sqe);
        }
    }

}
