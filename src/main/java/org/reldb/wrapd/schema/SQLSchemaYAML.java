package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;

/**
 * A SQLSchema with schema migrations defined in a YAML file.
 */
public class SQLSchemaYAML extends SQLSchema {
    final private Update[] updates;

    /**
     * Create an instance of a schema for a specified Database.
     *
     * @param yamlFileName YAML schema definition and migration file.
     * @param database Database.
     */
    public SQLSchemaYAML(Database database, String yamlFileName) {
        super(database);
        var inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(yamlFileName);
        ArrayList<?> migrations = new Yaml().load(inputStream);
        var updateList = new ArrayList<Update>();
        for (var migration: migrations) {
            if (migration instanceof String) {
                var migrationQuery = (String)migration;
                updateList.add(schema -> {
                    database.updateAll(migrationQuery);
                    return Result.OK;
                });
            }
            // TODO provide means to execute arbitrary Java code here?
        }
        updates = updateList.toArray(new Update[0]);
    }

    @Override
    protected Update[] getUpdates() {
        return updates;
    }
}
