Wrapd is a _SQL amplifier_ that makes SQL more powerful in Java. 

Rather than hiding SQL behind complexity or difficulty, it exposes SQL where it helps, hides SQL where it should, and makes SQL fast, safe and easy to integrate into Java while staying light, lean, and loveable.

Wrapd is for Java 11 and above.

See [https://github.com/DaveVoorhis/Wrapd-demo](https://github.com/DaveVoorhis/Wrapd-demo) for a Wrapd-ly written demo application.

The Wrapd library is hosted at [https://github.com/DaveVoorhis/Wrapd](https://github.com/DaveVoorhis/Wrapd)

*"Wrapd" is pronounced "wrapped", "rapid" and "rapt."*

* SQL access is helpfully *wrapped*, not awkwardly hidden.
* SQL-in-Java development is *rapid*.
* SQL focus can be *rapt*.
  
Advanced User? See [JavaDoc](javadoc/index.html){:target="_wrapd_javadoc"}

## Why Wrapd? ##

#### Easily and quickly define SQL queries in your DBMS's SQL syntax and invoke them via conventional Java methods. #### 

Each SQL query definition in Wrapd acts as:
   - A unit test to verify the query works; and
   - A code generator to automatically generate Java methods to invoke it.

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
              .forEach(row -> System.out.println("x = " + row.x + ", y = " + row.y));
```

In short, Wrapd provides a type-checked, statically-compiled bridge between JDBC and Java Streams.

#### Code generation is part of the Wrapd library ####

It is invoked with conventional Java and integrates easily into your build pipeline and requires no external tools.

#### Query definitions and the Java methods to invoke them can be organised into one or more database abstraction layers. ####

Define queries with SQL; run them with Java methods.

#### SQL text is exposed in the query definitions, but hidden in the query invocations. ####

At production run-time, queries are safe from inadvertent modification and safe from SQL injection.

#### SELECT queries generate Streams-friendly results. ####

Result columns are referenced as native instance variables.

#### Inserting and updating table rows is easy. ####

#### If needed, table names can be automatically prefixed. ####

Avoid table name collisions when multiple applications share a database.

#### Easy automatic schema migration. ####

Integrate schema migration into your application or deploy stand-alone a schema migrator.


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

### How to Use Wrapd -- Overview ##

1. Create your database. Wrapd is a "SQL first" library, so the assumption is that the database exists, either via Wrapd's schema migration mechanisms or externally defined.

2. Define your SQL queries inside Java. Collectively, your query definitions form a database abstraction layer. All the SQL lives within the database abstraction layer. Normally, no SQL is found outside it, because all queries will be invoked by regular type-safe Java methods.

3. Run the code generator to test the query definitions and generate Java source code from them and compile the source code to binary .class files.

4. Use the generated methods to invoke your previously-defined SQL queries with type-checked parameters and no visible SQL. SELECT queries emit Java Streams with native, statically type-checked attributes.

### Easy Queries ###


### Documentation and examples are a work-in-progress. Wrapd will soon be available on Maven Central. Watch this space! ###
