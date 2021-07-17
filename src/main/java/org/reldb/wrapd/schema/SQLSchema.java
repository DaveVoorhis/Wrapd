package org.reldb.wrapd.schema;

import org.reldb.toolbox.strings.Str;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database;

import java.sql.SQLException;

import static org.reldb.wrapd.il8n.Strings.ErrVersionTableIsEmpty;
import static org.reldb.wrapd.il8n.Strings.ErrVersionTableValueIsInvalid;

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
                return new VersionNewDatabase();
            var versionRaw = database.valueOfAll("SELECT " + getVersionTableAttributeName() + " FROM " + getVersionTableName(), getVersionTableAttributeName());
            if (versionRaw.isEmpty())
                return new VersionIndeterminate(Str.ing(ErrVersionTableIsEmpty, getVersionTableName()));
            var versionStr = versionRaw.get().toString();
            int version;
            try {
                version = Integer.valueOf(versionStr);
            } catch (NumberFormatException nfe) {
                return new VersionIndeterminate(Str.ing(ErrVersionTableValueIsInvalid, getVersionTableName(), getVersionTableAttributeName()));
            }
            return new VersionNumber(version);
        } catch (SQLException sqe) {
            return new VersionIndeterminate(sqe.toString());
        }
    }

    @Override
    protected UpdateTransaction getTransaction() {
        return new UpdateTransaction() {
            @Override
            public Result run(ResultAction action) {
                try {
                    return Result.BOOLEAN(database.processTransaction(transaction -> action.run().isOk()).success);
                } catch (SQLException sqe) {
                    return Result.ERROR(sqe);
                }
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
