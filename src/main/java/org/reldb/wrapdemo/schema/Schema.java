package org.reldb.wrapdemo.schema;

import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapdemo.database.mysql.GetDatabase;

import java.sql.SQLException;

public class Schema extends SQLSchema {
    /**
     * Create an instance of a schema for a specified Database.
     *
     * @param database Database.
     */
    public Schema(Database database) {
        super(database);
    }

    @Override
    protected Update[] getUpdates() {
        return new AbstractSchema.Update[] {
                schema -> {
                    getDatabase().updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
                    return Result.OK;
                },
                schema -> {
                    getDatabase().updateAll("CREATE TABLE $$tester02 (a INT NOT NULL PRIMARY KEY, b INT NOT NULL)");
                    return Result.OK;
                }
        };
    }

    public static void main(String args[]) {
        Schema schema = null;
        try {
            schema = new Schema(GetDatabase.getDatabase());
        } catch (SQLException e) {
            System.out.println("ERROR in Schema: main: GetDatabase.getDatabase(): " + e);
            return;
        }
        var result = schema.setup();
        if (result.isOk())
            System.out.println("OK: Schema has been set up.");
        else {
            System.out.println("ERROR: Schema creation: " + result.error);
        }
    }
}
