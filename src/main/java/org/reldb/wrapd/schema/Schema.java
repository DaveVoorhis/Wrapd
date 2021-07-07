package org.reldb.wrapd.schema;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.QueryDefinition;

public abstract class Schema {
    private Database database;
    private String codeDirectory;
    private String versionTableName;

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

    public QueryDefinition getVersionQuery() {
        return new QueryDefinition("__VersionQuery", "select version from " + getVersionTableName());
    }
}
