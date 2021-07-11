package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database;

import java.sql.SQLException;

public abstract class SQLSchema extends AbstractSchema {
    private final Database database;

    private String versionTableName = "$$__version";
    private String versionTableAttributeName = "version";
    private String versionTableAttributeTypeName = "integer";

    public SQLSchema(Database database) {
        this.database = database;
    }

    public void setVersionTableName(String versionTableName) {
        this.versionTableName = versionTableName;
    }

    public String getVersionTableName() {
        return versionTableName;
    }

    public void setVersionTableAttributeName(String versionTableAttributeName) {
        this.versionTableAttributeName = versionTableAttributeName;
    }

    public String getVersionTableAttributeName() {
        return versionTableAttributeName;
    }

    public void setVersionTableAttributeTypeName(String versionTableAttributeTypeName) {
        this.versionTableAttributeTypeName = versionTableAttributeTypeName;
    }

    public String getVersionTableAttributeTypeName() {return versionTableAttributeTypeName;}

    @Override
    public Version getVersion() {
        try {
            if (!database.isTableExists(getVersionTableName()))
                return new NewDatabase();
            var versionRaw = database.valueOfAll("SELECT " + getVersionTableAttributeName() + " FROM " + getVersionTableName(), getVersionTableAttributeName());
            if (versionRaw.isEmpty())
                return new Indeterminate("Versioning table " + getVersionTableName() + " is empty. It needs one row.");
            var versionStr = versionRaw.get().toString();
            int version;
            try {
                version = Integer.valueOf(versionStr);
            } catch (NumberFormatException nfe) {
                return new Indeterminate("Versioning table " + getVersionTableName() + " contains an invalid value for " + getVersionTableAttributeName() + ".");
            }
            return new Number(version);
        } catch (SQLException sqe) {
            return new Indeterminate(sqe.toString());
        }
    }

    @Override
    protected Result setVersion(Number number) {
        try {
            database.update("UPDATE " + getVersionTableName() + " SET " + getVersionTableAttributeName() + " = ?", number.value);
            return Result.OK;
        } catch (SQLException sqe) {
            return Result.ERROR(sqe);
        }
    }

    @Override
    protected Result createDatabase() {
        try {
            return Result.BOOLEAN(database.transact(xact -> {
                xact.updateAll("CREATE TABLE " + getVersionTableName() + "(" + getVersionTableAttributeName() + " " + getVersionTableAttributeTypeName() + ")");
                xact.updateAll("INSERT INTO " + getVersionTableName() + "(" + getVersionTableAttributeName() + ") VALUES (0)");
                return true;
            }));
        } catch (SQLException sqe) {
            return Result.ERROR(sqe);
        }
    }

}
