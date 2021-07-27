Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator that helpfully exposes
SQL in Java rather than hiding it.

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

Wrapd will generate classes and type-checked methods to conveniently invoke your queries. Run the query defined above like this:
```java
MyTableQuery01.query(database, 22, 88)
              .forEach(tuple -> System.out.println("x = " + tuple.x + ", y = " + tuple.y));
```

In short, Wrapd provides a type-checked, statically-compiled bridge between JDBC and Java Streams.

#### SQL Schemas are Easy ####

Version migrations/upgrades can be completely automated.

Create a new 'version 1' database:
```java
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

Later, add a migration from version 1 to version 2:
```java
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

Use cases:
* Embed schema migrations in your applications to automatically create or update their local
databases on startup.
* Run standalone schema migrations on servers to create or update their databases during new-version deployment.

In short, Wrapd makes it easy to deploy database creation and database upgrades.

### Rationale ###

Wrapd is a bridge between SQL and Java, and specifically between JDBC and Java Streams, to reduce the pain of
the usual SQL-in-Java pain points. That includes avoiding hulking, complicated, awkward ORMs.

There is intentionally no attempt to hide SQL. You use it the way you usually use it in JDBC. Instead, Wrapd exposes it
appropriately -- only in a database abstraction layer -- and make it easy to use SQL query results with Java Streams.

That, plus automated schema migration, is Wrapd's main selling point: JDBC-to-Streams, bridged.

First, you create your database. This is a "SQL first" library, where the presumption is that the database exists,
either via Wrapd's schema migration mechanisms or externally defined.

Then, you define your SQL queries inside Java. Collectively, your query definitions form a database abstraction layer.
All the SQL lives within the database abstraction layer. Normally, no SQL is found outside it, and all queries are
invoked by regular type-safe Java methods.

You run some simple machinery (not shown here) to iterate all the query definitions and generate Java source code from
them and compile the source code to binary .class files.

That creates ready-to-use methods to invoke your previously-defined SQL queries with
type-checked parameters and no visible SQL -- that's back in the query definitions -- that emit Java Streams with native,
compile-time type-checked attributes.

That's SQL, amplified, in Java.

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
