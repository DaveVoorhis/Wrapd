# SQL Schemas are Easy

Version migrations/upgrades can be completely automated.

Create a new 'version 1' database:
``` java
var schema = new SQLSchema(database) {
    protected AbstractSchema.Update[] getUpdates() {
        return new AbstractSchema.Update[] {
            // version 1
            schema -> {
                database.updateAll("CREATE TABLE mytab01 (x INT NOT NULL PRIMARY KEY)");
                return Result.OK;
            }
        };
    }
};
schema.setup();
```
When ```schema.setup()``` is run:
- If the database doesn't exist, it's created and brought up to version 1.
- If the database is already version 1, there is no schema change.

Later, add a migration from version 1 to version 2:
``` java
var schema = new SQLSchema(database) {
    protected AbstractSchema.Update[] getUpdates() {
        return new AbstractSchema.Update[] {
            // version 1
            schema -> {
                database.updateAll("CREATE TABLE mytab01 (x INT NOT NULL PRIMARY KEY)");
                return Result.OK;
            },
            // migration to version 2
            schema -> {
                database.updateAll("CREATE TABLE mytab02 (a INT NOT NULL PRIMARY KEY)");
                return Result.OK;
            }
        };
    }
};
schema.setup();
```
When ```schema.setup()``` is run:
- If the database doesn't exist, it's created and brought up to version 2.
- If the database is version 1, it's brought up to version 2.
- If the database is version 2, there is no schema change.

The schema version is stored in the database, 
so schema migrations can be done without any need for external version tracking.

Use cases:
* Embed schema migrations in your applications to automatically create or update their local
  databases on startup.
* Run standalone schema migrations on servers to create or update their databases during new-version deployment.

In short, Wrapd makes it easy to deploy database creation and database upgrades.

----
[Home](index.md)