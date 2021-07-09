package org.reldb.wrapd.schema;

import org.reldb.wrapd.sqldb.Database;

import java.sql.SQLException;

public abstract class Schema extends AbstractSchema {
    private Database database;
    private String codeDirectory;
    private String versionTableName = "$$__version";
    private String versionTableAttributeName = "version";
    private String versionTableAttributeTypeName = "integer";

    public Schema(Database database, String codeDirectory) {
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
        return null;
    }

    @Override
    protected void setVersion(Number number) {

    }

    @Override
    protected void createDatabase() {

    }

    public void latest() throws SQLException {
        final int version;
        if (database.isTableExists(getVersionTableName())) {
            var versionRaw = database.valueOfAll("select " + getVersionTableAttributeName() + " from " + getVersionTableName(), getVersionTableAttributeName());
            if (versionRaw.isEmpty()) {
                // TODO - replace with appropriate il8n-able string.
                // TODO - use streams syntax for this
                throw new SQLException("Versioning table " + getVersionTableName() + " is empty. It needs one row.");
            }
            var versionStr = versionRaw.get().toString();
            try {
                version = Integer.valueOf(versionStr);
            } catch (NumberFormatException nfe) {
                // TODO - replace with appropriate il8n-able string.
                throw new SQLException("Versioning table " + getVersionTableName() + " contains an invalid value for " + getVersionTableAttributeName() + ".");
            }
        } else {
            if (!database.transact(xact -> {
                xact.updateAll("create table " + getVersionTableName() + "(" + getVersionTableAttributeName() + " " + getVersionTableAttributeTypeName() + ")");
                xact.updateAll("insert into " + getVersionTableName() + "(" + getVersionTableAttributeName() + ") values (0)");
                return true;
            }))
                // TODO - replace with appropriate il8n-able string.
                throw new SQLException("Unable to create or initialise versioning table " + getVersionTableName() + ".");
            version = 0;
        }
        var changes = getChanges();
        if (changes.length > 0) {
                for (int change = version; change < changes.length; change++) {
                    final int changeNumber = change;
                    final int newVersion = change + 1;
                    database.transact(xact -> {
                        // TODO handle changes[changeNumber]
                        xact.update("update " + getVersionTableName() + " set " + getVersionTableAttributeName() + " = ?", newVersion);
                        return true;
                    });
            };
        }
    }
}
