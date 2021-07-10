package org.reldb.wrapd.schema;

import org.reldb.wrapd.sqldb.Database;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SQLSchema extends AbstractSchema {
    private static Logger LOG = Logger.getLogger(SQLSchema.class.getName());

    private Database database;
    private String codeDirectory;
    private String versionTableName = "$$__version";
    private String versionTableAttributeName = "version";
    private String versionTableAttributeTypeName = "integer";

    public SQLSchema(Database database, String codeDirectory) {
        this.database = database;
        this.codeDirectory = codeDirectory;
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
            LOG.log(Level.SEVERE, "Unable to get version", sqe);
            return new Indeterminate(sqe.toString());
        }
    }

    @Override
    protected boolean setVersion(Number number) {
        try {
            database.update("UPDATE " + getVersionTableName() + " SET " + getVersionTableAttributeName() + " = ?", number.get());
            return true;
        } catch (SQLException sqe) {
            LOG.log(Level.SEVERE, "Unable to update version", sqe);
            return false;
        }
    }

    @Override
    protected boolean createDatabase() {
        try {
            if (!database.transact(xact -> {
                xact.updateAll("CREATE TABLE " + getVersionTableName() + "(" + getVersionTableAttributeName() + " " + getVersionTableAttributeTypeName() + ")");
                xact.updateAll("INSERT INTO " + getVersionTableName() + "(" + getVersionTableAttributeName() + ") VALUES (0)");
                return true;
            })) {
                LOG.log(Level.SEVERE, "Unable to create database");
                return false;
            }
            return true;
        } catch (SQLException sqe) {
            LOG.log(Level.SEVERE, "Unable to create database", sqe);
            return false;
        }
    }

}
