A _SQL amplifier_ makes SQL easier to use, better integrated, better tested, 
more easily migrated and managed than using alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

Wrapd is for Java 11 and above.

See [https://github.com/DaveVoorhis/Wrapd-demo](https://github.com/DaveVoorhis/Wrapd-demo) for a Wrapd-ly built demo application.

The Wrapd library is hosted at [https://github.com/DaveVoorhis/Wrapd](https://github.com/DaveVoorhis/Wrapd)

*"Wrapd" is pronounced "wrapped", "rapid" and "rapt."*

* SQL access is helpfully *wrapped*, not awkwardly hidden.
* SQL-in-Java development is *rapid*.
* SQL focus can be *rapt*.

### Key Features ###

#### SQL Queries are Easy ####

Predefine SQL queries like this, which tests the queries and automatically generates the database access layer:
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

Use cases:
* Embed schema migrations in your applications to automatically create or update their local
  databases on startup.
* Run standalone schema migrations on servers to create or update their databases during new-version deployment.

In short, Wrapd makes it easy to deploy database creation and database upgrades.

### Rationale ###

Wrapd is a bridge between SQL and Java, particularly between JDBC and Java Streams, to reduce the pain of
the usual SQL-in-Java pain points. That includes avoiding hulking, complicated, awkward ORMs.

There is intentionally no attempt to hide SQL. Instead, Wrapd exposes it
appropriately -- but preferably _only_ in the database abstraction layer -- and
makes it easy to use SQL query results with Java Streams.

That -- plus automated schema migration -- is Wrapd's main selling point: JDBC-to-Streams, bridged.

It's easy:

1. Create your database. Wrapd is a "SQL first" library, where the presumption is that the database exists,
   either via Wrapd's schema migration mechanisms or externally defined.

2. Define your SQL queries inside Java. Collectively, your query definitions form a database abstraction layer.
   All the SQL lives within the database abstraction layer. Normally, no SQL is found outside it, because all queries will be invoked by regular type-safe Java methods.

3. Run some simple machinery (not shown here) to iterate all the query definitions and generate Java source code from
   them and compile the source code to binary .class files.

4. Use the generated methods to invoke your previously-defined SQL queries with
   type-checked parameters and no visible SQL (that's in the query definitions, in
   the database abstraction layer) which emit Java Streams with native, statically type-checked
   attributes.

That's SQL, amplified, in Java.

### Documentation and examples are a work-in-progress. Wrapd will soon be available on Maven Central. Watch this space! ###
