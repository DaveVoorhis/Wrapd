Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator to make using SQL in Java easy
by exposing it the right way rather than hiding it.

Highly opinionated, Wrapd doesn't hide SQL from you. Instead, it makes SQL easier 
to use and better integrated, tested, migrated and managed than using 
alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

*"Wrapd" is pronounced "wrapped", "rapid" and "rapt."*

* SQL access is helpfully *wrapped*, not awkwardly hidden.
* SQL-in-Java development is *rapid*.
* SQL focus can be *rapt*.

### Key Features ###

#### SQL Queries are Easy ####

Predefine SQL queries like this, which tests the queries and automatically generates the database access layer Java code to use them:
```java
public class MyQueryDefinitions extends QueryDefiner {
      ...
      public QueryDefinition QueryDefinition01() {
          return new QueryDefinition("MyTableQuery01", 
              "SELECT * FROM mytable WHERE x > ? AND x < ?", 3, 7);
      }
      ...
}
```
Note the example arguments, which are used to test the query and determine its parameter and result types.

Wrapd will generate classes and type-checked methods to invoke your queries easily. Run the query defined above like this:
```java
MyTableQuery01.query(database, 22, 88)
              .forEach(tuple -> System.out.println("x = " + tuple.x + ", y = " + tuple.y));
```

In short, Wrapd provides a type-checked, statically-compiled bridge between JDBC and Java Streams.

#### SQL Schemas are Easy ####

Version migrations/upgrades can be completely automated.

Create a new 'version 1' database:
```java
var schema = new TestSchema(database) {
    protected AbstractSchema.Update[] getUpdates() {
        return new AbstractSchema.Update[] {
            // version 1
            schema -> {
                database.updateAll("CREATE TABLE mytab01 (x INT NOT NULL PRIMARY KEY)");
                return Result.OK;
            }
        };
    }
}
schema.setup();
```

Later, add a migration from version 1 to version 2:
```java
var schema = new TestSchema(database) {
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

Use cases:
* Embed schema migrations in your applications to automatically create or update their local
databases on startup.
* Run standalone schema migrations on servers to create or update their databases during new-version deployment.

In short, Wrapd makes it easy to deploy database creation and database upgrades.

### To build Wrapd ###

1.   _gradle clean_
2.   _gradle publishToMavenLocal_

### To test Wrapd ###

You need Docker installed on your system, because Docker images of popular DBMSs are used to perform end-to-end tests.

1.  _docker-compose up -d_
2.  _gradle clean_
3.  _gradle test_
4.  _docker-compose down -v_
 
### Documentation and examples are a work-in-progress. Watch this space! ###
