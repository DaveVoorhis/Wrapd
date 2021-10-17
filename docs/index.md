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

## What is Wrapd? ##

An annotation-free, pure Java library that:
1. Lets you define SQL queries in your Java code, tests them, and generates Java code to invoke them as conventional, Streams-compatible, SQL-injection-avoiding, high performance, statically-type-checked method invocations in your Java applications.
2. Makes it easy to manage and migrate schema changes from your Java applications.

It turns SQL query definitions like this:

```java
defineQuery("JoinABCXYZWhere", 
  "SELECT * FROM $$ABC, $$XYZ WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
```

Into tested, re-usable, type-safe, SQL-injection-free, Streams-compatible Java method invocations like this:

```java
joinABCXYZWhere(1002, 1008)
        .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c +
                " x = " + row.x + " y = " + row.y + " z = " + row.z));
```

Whilst keeping your code simple, straightforward, and the database schema in sync.

## Why Wrapd? ##

### Features ###

1. SQL queries are defined in Java, unit tested, and invoked with conventional type-safe Java methods.
2. Code generation is part of the Wrapd library and integrates into your build pipeline. No external tools are required.
3. Database access is kept in database abstraction layers to increase cohesion, reduce coupling, and promote separation of concerns.
4. SQL text is exposed in query definitions but hidden in query invocations, making them safe, secure, and SQL-injection-free.
5. SELECT queries generate Streams-friendly results. Java integration is easy and straightforward.
6. Inserting and updating table rows is easy.
7. If needed, table names can be automatically prefixed to avoid table name collisions in shared databases.
8. Easy, automatic schema migration keeps your code in sync with your database schema and vice versa.

Wrapd is SQL _amplified_, not obscured!

### Details ###

#### SQL queries are defined in Java, unit tested, and invoked with conventional type-safe Java methods. #### 

Each SQL query definition in Wrapd acts as:
   - A unit test to run the query and verify that it works; and
   - A code generator to automatically generate Java methods to invoke it.

Predefine SQL queries like this, which tests the queries and automatically generates a database access layer:
```java
package org.reldb.wrapd.demo;

import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.demo.mysql.GetDatabase;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Definer;

public class Definitions extends Definer {

    public Definitions(Database database, String codeDirectory, String packageSpec) {
        super(database, codeDirectory, packageSpec);
    }

    void generate() throws Throwable {
        purgeTarget();

        defineTable("$$ABC");
        defineTable("$$XYZ", "x = {xValue}", 22);
        defineQueryForTable("SelectABCWhere", "$$ABC", "SELECT * FROM $$ABC WHERE a = {aValue}", 22);
        defineQuery("JoinABCXYZ", "SELECT * FROM $$ABC, $$XYZ WHERE x = a");
        defineQuery("JoinABCXYZWhere", "SELECT * FROM $$ABC, $$XYZ WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
        defineUpdate("ClearABC", "DELETE FROM $$ABC");
        defineUpdate("ClearXYZ", "DELETE FROM $$XYZ");
        defineUpdate("ClearABCWhere", "DELETE FROM $$ABC WHERE a = {aValue}", 3);
        defineValueOf("ValueOfABCb", "SELECT b FROM $$ABC");
        defineValueOf("ValueOfXYZz", "SELECT z FROM $$XYZ WHERE x = {xValue}", 33);

        emitDatabaseAbstractionLayer("DatabaseAbstractionLayer");
    }

    public static void main(String[] args) throws Throwable {
        var db = GetDatabase.getDatabase();
        var codeDirectory = "src/main/java";
        var codePackage = "org.reldb.wrapd.demo.generated";
        if (!Directory.chkmkdir(codeDirectory)) {
            System.out.println("ERROR creating code directory " + codeDirectory);
            return;
        }
        var sqlDefinitions = new Definitions(db, codeDirectory, codePackage);
        sqlDefinitions.generate();
        System.out.println("OK: Queries are ready.");
    }
}
```
Note the example arguments, which are used to test parametric queries and determine its parameter and result types.

Wrapd will generate classes and type-checked methods to conveniently invoke your queries. Run the queries defined above like this:
```java
package org.reldb.wrapd.demo;

import org.reldb.wrapd.demo.generated.*;
import org.reldb.wrapd.demo.mysql.GetDatabase;

import java.sql.SQLException;

public class Application {

    private static class Demo extends DatabaseAbstractionLayer {

        public Demo() throws Exception {
            super(GetDatabase.getDatabase());
        }

        void populateABC() throws SQLException {
            for (var i = 1000; i < 1010; i++) {
                var tuple = new ABCTuple(getDatabase());
                tuple.a = i;
                tuple.b = i * 2;
                tuple.c = Integer.toString(i * 10);
                tuple.insert();
            }
        }

        void populateXYZ() throws SQLException {
            for (var i = 1005; i < 1015; i++) {
                var tuple = new XYZTuple(getDatabase());
                tuple.x = i;
                tuple.y = i * 2;
                tuple.z = Integer.toString(i * 100);
                tuple.insert();
            }
        }

        void run() throws Exception {
            System.out.println("== ClearABC ==");
            clearABC();
            System.out.println("== ClearXYZ ==");
            clearXYZ();
            System.out.println("== populateABC ==");
            populateABC();
            System.out.println("== populateXYZ ==");
            populateXYZ();
            System.out.println("== ABC ==");
            aBC().forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
            System.out.println("== XYZ (1007) ==");
            xYZ(1007)
                    .forEach(row -> System.out.println("Row: x = " + row.x + " y = " + row.y + " z = " + row.z));
            System.out.println("== ClearABCWhere (1007) ==");
            clearABCWhere(1007);
            System.out.println("== ABC ==");
            aBC().forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
            System.out.println("== ABC - queryForUpdate row.b += 100 ==");
            aBCForUpdate()
                    .forEach(row -> {
                        row.b += 100;
                        try {
                            row.update();
                        } catch (SQLException e) {
                            System.out.println("Row update failed due to: " + e);
                        }
                    });
            System.out.println("== ABC ==");
            aBC().forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
            System.out.println("== JoinABCXYZ ==");
            joinABCXYZ()
                    .forEach(row -> System.out.println("Row:" +
                            " a = " + row.a + " b = " + row.b + " c = " + row.c +
                            " x = " + row.x + " y = " + row.y + " z = " + row.z));
            System.out.println("== JoinABCXYZWhere (1002, 1008) ==");
            joinABCXYZWhere(1002, 1008)
                    .forEach(row -> System.out.println("Row:" +
                            " a = " + row.a + " b = " + row.b + " c = " + row.c +
                            " x = " + row.x + " y = " + row.y + " z = " + row.z));
            System.out.println("== ValueOfABCb ==");
            var valueOfABCb = valueOfABCb();
            System.out.println(valueOfABCb.isPresent() ? valueOfABCb.get() : "?");
            System.out.println("== ValueOfXYZz ==");
            System.out.println(valueOfXYZz(1007).orElse("?"));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("test"))
            return;
        new Demo().run();
    }
}
```

#### Code generation is part of the Wrapd library and integrates into your build pipeline. ####

Code generation steps are invoked with conventional Java, and can be easily integrated into your build pipeline with no external tools or special plugins.

In your project Java source:
```java
 // Generate SQL-invocation methods
 public static void main(String[] args) throws Throwable {
     var db = GetDatabase.getDatabase();
     var codeDirectory = "src/main/java";
     var codePackage = "org.reldb.wrapd.demo.generated";
     var sqlDefinitions = new Definitions(db, codeDirectory, codePackage);
     sqlDefinitions.generate();
     System.out.println("OK: Queries are ready.");
 }
```

In your project build.gradle (assuming Gradle build pipeline):
```groovy
plugins {
    id 'org.reldb.wrapd.demo.repositories_and_dependencies_with_database'
}

task runQueryBuild(type: JavaExec) {
    group = "Wrapd"
    description = "Generate code from query definitions."
    classpath = sourceSets.main.runtimeClasspath
    mainClass = "org.reldb.wrapd.demo.Definitions"
}
```

That exposes a Gradle task called _runQueryBuild_. Run it to turn the SQL query definitions into invocable Java methods.

#### Database access is kept in database abstraction layers. ####

The _emitDatabaseAbstractionLayer_ method emits a class definition that includes
all the previously-defined queries as methods.
```java
...
defineQuery("JoinABCXYZWhere", 
   "SELECT * FROM $$ABC, $$XYZ WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
defineValueOf("ValueOfXYZz", "SELECT z FROM $$XYZ WHERE x = {xValue}", 33);

emitDatabaseAbstractionLayer("DatabaseAbstractionLayer");
```

Invoke them like this:

```java
 private static class Demo extends DatabaseAbstractionLayer {

     public Demo(Database database) {
         super(database);
     }

     public void run() throws Exception {
         joinABCXYZWhere(1002, 1008)
            .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c +
                                               " x = " + row.x + " y = " + row.y + " z = " + row.z));
         System.out.println(valueOfXYZz(1007).orElse("?"));
     }
 }
```

This increases cohesion between related queries whilst 
reducing coupling between Java application code and SQL text, and promotes separation of concerns.

#### SQL text is exposed in query definitions but hidden in query invocations. ####

At run-time, queries are safe from inadvertent modification and safe from SQL injection.

What you define is:

```java
defineQuery("JoinABCXYZWhere", "SELECT * FROM $$ABC, $$XYZ WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
```

What you run is:

```java
joinABCXYZWhere(1002, 1008)
       .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c +
               " x = " + row.x + " y = " + row.y + " z = " + row.z));
```

SQL text is confined to definitions, not exposed in invocations, tested and statically type-safe. 
Internally, parametric queries are implemented as prepared statements. SQL text is _not_ dynamically generated.

#### SELECT queries generate Streams-friendly results. ####

Result columns are referenced as native instance variables.

```java
joinABCXYZWhere(1002, 1008)
       .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c +
               " x = " + row.x + " y = " + row.y + " z = " + row.z));
```

Note how attributes of the result set are accessed as native Java instance variables 
in a statically-compiled, type-safe, Streams-compatible manner.

#### Inserting and updating table rows is easy. ####

Insert a row:
```java
var tuple = new XYZTuple(database);
tuple.x = i;
tuple.y = i * 2;
tuple.z = Integer.toString(i * 100);
tuple.insert();
```

Update rows:
```java
aBCForUpdate().forEach(row -> {
    row.b += 100;
    try {
        row.update();
    } catch (SQLException e) {
        System.out.println("Row update failed due to: " + e);
    }
});
```

#### If needed, table names can be automatically prefixed. ####

Avoid table name collisions when multiple applications share a database.

Note how the table names in this example are prefixed with '$$':
```java
defineQuery("JoinABCXYZWhere", 
  "SELECT * FROM $$ABC, $$XYZ WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
```

Wherever $$ appears in SQL query text, it will be replaced with a predefined string.

This is an optional feature. If you don't need it, simply define queries with explicit table names:

```java
defineQuery("JoinABCXYZWhere", 
  "SELECT * FROM ABC, XYZ WHERE x = a AND x > {lower} AND x < {higher}", 2, 5);
```

#### Easy automatic schema migration. ####

Integrate schema migration into your application or deploy stand-alone a schema migrator.

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

This means you can:
* Embed schema migrations in your applications to automatically create or update their local
  databases on startup.
* Run standalone schema migrations on servers to create or update their databases during new-version deployment.

In short, Wrapd makes it easy to deploy database creation and database upgrades.

## How to Use Wrapd #

### Overview ###

1. Create your database. Wrapd is an "SQL first" library, so the assumption is that the database exists, either via Wrapd's schema migration mechanisms or externally defined.

2. Define your SQL queries in your project. A collection of query definitions specifies a database abstraction layer.

3. Run the code generator to test the query definitions and generate Java source code to invoke them.

4. Use the generated methods to run your previously-defined SQL queries with type-checked parameters and no visible SQL. SELECT queries emit Java Streams with native, statically type-checked attributes.

### Creating a Wrapd Project ###

It is recommended to organise your Wrapd project as at least three main subprojects:

1. A *schema* subproject to create/update the SQL schema. This is optional.
2. A *queries* subproject to convert SQL query definitions into Java methods, organised as one or more database abstraction layers.
3. An *application* subproject to host the application.

You may want a fourth *database* subproject to manage database connectivity, as it will be shared by the three main subprojects above.

Dividing a Wrapd project into (at least) these three main subprojects makes it possible to:
1. Convert SQL query definitions into Java methods without being blocked by compilation failures in the application(s).
2. Avoid deploying the SQL query definitions, as they're not needed in production.
3. Deploy schema updates independently of the application(s), if desired.

You _can_ integrate them all into one project without subprojects, but you will almost certainly
encounter difficulties with compilation failures as you migrate schemas and change queries, unless you are careful
to delete the Java code for obsolete query definitions and (possibly) comment out the application 
code that uses them.

It's generally _much_ easier to divide the project into subprojects as described above.

### The Wrapd-demo Project ###

The [Wrapd-demo](https://github.com/DaveVoorhis/Wrapd-demo) 
demonstration application has been built according the above structure. It consists of three main subprojects...
1. *schema* - to create/update the SQL schema.
2. *queries* - to convert SQL query definitions into Java methods, organised as a database abstraction layer.
3. *application* - the application.

...plus two additional subprojects:
1. *database* - database connectivity shared by the above, including a demonstration of connection pooling because most real-world applications will need it.
2. *buildSrc* - Gradle project settings.

Perhaps the easiest way to build a new Wrapd application is to simply copy the Wrapd-demo demonstration application and modify it to suit your requirements.

That said, a step-by-step tutorial for building a Wrapd project will soon be coming to this space.

### Documentation and examples are a work-in-progress. Wrapd will soon be available on Maven Central. Watch this space! ###
