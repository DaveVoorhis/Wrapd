Creating a Wrapd Project - With Gradle
======================================

## Step 1 - Get the Tools ##

Make sure you have the required tools for this tutorial:

1. Java JDK 11+
2. Gradle 7.1+
3. MySQL 5+. For tutorial purposes we'll use MySQL, though Wrapd can work with almost any JDBC-compatible DBMS and has automated tests for PostgreSQL, SQLite, MS SQL Server, and MySQL. If this is your first time through this tutorial, it may be easiest to set up a MySQL DBMS using Docker as described below.

### (Optional) Use Docker to Run a MySQL DBMS ###

Install Docker and create a Docker Compose file:
1. Install Docker from https://docker.com
2. Create a suitable directory and create a file called docker-compose.yml with the following content:

```yaml
version: '3.9'

services:
  mysql-db:
    image: mysql:8.0.25
    command: --default-authentication-plugin=mysql_native_password
    restart: always
    volumes:
      - db-data-wrapd_mysql_myproject:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: wrapd_myproject
      MYSQL_USER: user
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"

volumes:
  db-data-wrapd_mysql_myproject:
    driver: local
```

To launch the Docker MySQL instance:
1. Change to the directory containing docker-compose.yml
2. Run: ```docker-compose up -d```
   
To shut down the Docker MySQL instance and retain the database for next time:
1. Change to the directory containing docker-compose.yml
2. Run: ```docker-compose down```

To shut down the Docker MySQL instance and delete the database:
1. Change to the directory containing docker-compose.yml
2. Run: ```docker-compose down -v```

## Step 2 - Start a Gradle Project ##

Create a directory to host your project. Change to it.

Type ```gradle init``` and press Enter.

1. For project type, choose "2: application".
2. For implementation language, choose "3: Java". 
3. In response to "Split functionality across multiple subprojects?" choose "2: yes - application and library projects". 
4. For the build script DSL, choose "1: Groovy". 
5. Pick whatever project name you like. 
6. For "Source package", put *org.reldb.myproject*. You can, of course, use whatever package you like, but for tutorial purposes we'll use *org.reldb.myproject*.

Your project skeleton will now be created. Get a directory listing to see what it created. You should see something like this:

```
drwxr-xr-x  13 dave  staff   416 26 Oct 21:40 .
drwxr-xr-x   4 dave  staff   128 26 Oct 21:33 ..
-rw-r--r--   1 dave  staff   154 26 Oct 21:40 .gitattributes
-rw-r--r--   1 dave  staff   103 26 Oct 21:40 .gitignore
drwxr-xr-x   6 dave  staff   192 26 Oct 21:36 .gradle
drwxr-xr-x   4 dave  staff   128 26 Oct 21:40 app
drwxr-xr-x   4 dave  staff   128 26 Oct 21:40 buildSrc
drwxr-xr-x   3 dave  staff    96 26 Oct 21:36 gradle
-rwxr-xr-x   1 dave  staff  8070 26 Oct 21:36 gradlew
-rw-r--r--   1 dave  staff  2763 26 Oct 21:36 gradlew.bat
drwxr-xr-x   4 dave  staff   128 26 Oct 21:40 list
-rw-r--r--   1 dave  staff   392 26 Oct 21:40 settings.gradle
drwxr-xr-x   4 dave  staff   128 26 Oct 21:40 utilities
```

If you created a docker-compose.yml file above, you may want to move it to this project directory.

Some things to note:
- As a "multiple subprojects" project, Gradle has created *utilities* and *list* as examples of library subprojects and *app* as the application subproject. We'll rename them shortly. 
- The *buildSrc* subproject, which often contains imperative custom build logic, can also contain settings common to some of or all the other subprojects. 
- The *gradle* & *.gradle* directories and *gradlew* & *gradlew.bat* files -- along with *buildSrc* -- provide Gradle build scripts and infrastructure. 
- The *settings.gradle* file defines top-level settings, such as what subprojects should be built. 
- The *.gitattributes* and *.gitignore* files contain handy defaults if you're using git to manage your source code.

## Step 3 - Create a Database Subproject ##

Connecting to a database, even an empty one, is fundamental to Wrapd's way of working. 
You need to start with a (possibly empty) database. You can use your own or the Docker-based MySQL DBMS instance
described above, but the remainder of this tutorial assumes you're using the Docker-based MySQL DBMS instance, or at least equivalent
settings and credentials with your own database.

First, let's turn the *utilities* subproject into a *database* subproject. It will provide connectivity
to a SQL DBMS, and will be used by the other subprojects.

1. Rename *utilities* to *database*. This renames the subproject directory.
2. Edit *settings.gradle* and change *utilities* to *database*. This tells Gradle to build the *database* subproject.
3. Rename *database/src/main/java/org/reldb/myproject/utilities* to *database/src/main/java/org/reldb/myproject/database*. This renames the formerly-*utilities* directory to a more appropriate *database*.
4. Delete the sample auto-generated files in *database/src/main/java/org/reldb/myproject/database*.
5. Edit *app/build.gradle* and change "utilities" in the following to "database":

   ```groovy
   dependencies {
       implementation 'org.apache.commons:commons-text'
       implementation project(':utilities')
   }
   ```
   
   It should now be:

   ```groovy
   dependencies {
       implementation 'org.apache.commons:commons-text'
       implementation project(':database') 
   }
   ```

   This tells the *app* subproject to reference the *database* subproject. The 'org.apache.commons:commons-text' dependency won't be used in this tutorial.

Now try ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL.

## Step 4 - Configure the Database Subproject ##

1. In *database/build.gradle*, replace the following...

   ```groovy
   dependencies {
       api project(':list')
   }
   ```
   
   ...with...

   ```groovy
   dependencies {
       implementation 'org.reldb:Wrapd:1.0.0'
       implementation 'mysql:mysql-connector-java:8.0.27'
       testImplementation 'org.junit.jupiter:junit-jupiter:5.7.2'
       testImplementation 'org.junit.platform:junit-platform-runner:1.7.2'
   }
   ```
   
   This declares that the database subproject depends on:
   - Wrapd 1.0.0
   - MySQL Connector 8.0.27
   - The JUnit test framework (but only for testing)


2. Put the following in *database/src/main/java/org/reldb/myproject/GetDatabase.java*, to define the GetDatabase class with a getDatabase() method for obtaining Database instances.

   ```java
   package org.reldb.myproject.database;

   import com.mysql.cj.jdbc.MysqlDataSource;
   import org.reldb.wrapd.sqldb.Database;
   
   import java.io.InputStream;
   import java.util.Properties;
   
   public class GetDatabase {
   
       private static final String PROPERTIES_NAME = "db.properties";
   
       public static Database getDatabase() throws Exception {
           try (InputStream propertiesSource = GetDatabase.class.getClassLoader().getResourceAsStream(PROPERTIES_NAME)) {
               var properties = new Properties();
               if (propertiesSource == null)
                   throw new Exception("Missing " + PROPERTIES_NAME);
               properties.load(propertiesSource);
               var dataSource = new MysqlDataSource();
               dataSource.setURL(properties.getProperty("db.url"));
               dataSource.setUser(properties.getProperty("db.user"));
               dataSource.setPassword(properties.getProperty("db.password"));
               var tableNamePrefix = properties.getProperty("db.tablename_prefix", "");
               return new Database(
                   dataSource,
                   tableNamePrefix,
                   null
               );
           }
       }
   }
   ```

3. Put the following in *database/src/main/resources/db.properties* to define the database settings.

   ```properties
   db.tablename_prefix=wrapd_myproject
   db.url=jdbc:mysql://localhost/wrapd_myproject
   db.user=user
   db.password=password
   ```
   
   These values work with the Docker MySQL database defined in the *docker-compose.yml* file at the start of this document. If you're using your own MySQL DBMS, change as appropriate. If you're using a MySQL DBMS other than the Docker instance, you will need to create the database beforehand.

4. Put the following in *database/src/test/java/org/reldb/myproject/TestGetDatabase.java* to test your database connection.

   ```java
   package org.reldb.myproject.database;
   
   import org.junit.jupiter.api.Test;
   
   import static org.junit.jupiter.api.Assertions.fail;
   
   public class TestGetDatabase {
       @Test
       void verifyDatabaseConnectionSuccessful() {
           try {
               GetDatabase.getDatabase();
           } catch (Exception e) {
               e.printStackTrace();
               fail("Unable to connect to database.", e);
           }
       }
   }
   ```

5. Delete everything in:
   - *app/src/main/java/org/reldb/myproject/app/*
   - *app/src/test/java/org/reldb/myproject/app/*

   This is example code that was auto-generated by Gradle and is no longer needed.

6. Launch the Docker MySQL DBMS container, as described at the top of this document.

7. Run ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL.

## Step 5 - Create a Schema Subproject ##

Wrapd contains a simple but effective schema migrator that can be used to automate schema initialisation and upgrades on multiple target databases. This avoids the error-prone complexity of manually managing schema updates.

Well turn the *list* subproject into a *schema* subproject. It will create the initial schema for this tutorial,
and demonstrate how schema changes can be migrated. Later, we'll copy it to a *queries* subproject to become the basis for the main feature
of Wrapd: turning tested SQL queries into Java methods to invoke them.

1. Rename *list* to *schema*. This renames the subproject directory.
2. Edit *settings.gradle* and change *list* to *schema*. This tells Gradle to build the *schema* subproject.
3. Rename *schema/src/main/java/org/reldb/myproject/list* to *database/src/main/java/org/reldb/myproject/schema*. This renames the formerly-*utilities* directory to a more appropriate *database*.
4. Delete the sample auto-generated files in *schema/src/main/java/org/reldb/myproject/database*.
5. Delete the directory *schema/src/test*. We won't need it for now.
6. Edit *schema/build.gradle* and add the following after the *plugins* block:
   
   ```groovy
   dependencies {
     implementation project(':database')
     implementation 'org.reldb:Wrapd:1.0.0'
   }
   ```

   This tells the *schema* subproject to reference the *database* subproject because it will need to connect to the database. It's also dependent on Wrapd.

Now try ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL.

## Step 6 - Configure the Schema Subproject ##

In *schema/src/main/java/org/reldb/myproject/schema* create a file called Schema.java with the following content:

```java
package org.reldb.myproject.schema;

import org.reldb.toolbox.progress.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Response;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.sqldb.Database;

import org.reldb.myproject.database.GetDatabase;

public class Schema extends SQLSchema {
    public Schema(Database database) {
        super(database);
    }

    @Override
    protected Update[] getUpdates() {
        return new Update[] {
             schema -> {
                 getDatabase().updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
                 return Result.OK;
             },
        };
    }

    public static void main(String[] args) throws Exception {
        var schema = new Schema(GetDatabase.getDatabase());
        var result = schema.setup(new ConsoleProgressIndicator());
        if (result.isOk())
            System.out.println("OK: Schema has been set up.");
        else
            Response.printError("ERROR in Schema: Schema creation:", result.error);
    }
}
```

Now add the following to the end of *schema/gradle.build*:

```groovy
task runSchemaSetup(type: JavaExec) {
    group = "Wrapd"
    description "Ensure that the schema is up-to-date."
    classpath = sourceSets.main.runtimeClasspath
    mainClass = "org.reldb.myproject.Schema"
}
```

That adds a Gradle task called 'runSchemaSetup' that will run the Schema main(...) method to generate the schema. It can be run as often as you like, as it will only build a new schema if needed.

Run ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL.

Now make sure your MySQL DBMS instance is running and run ```gradle runSchemaSetup``` to generate the initial schema. You should see something like:

```
...
> Task :schema:runSchemaSetup
Creating schema: 0.0% complete.
Schema created: 50.0% complete.
Updating to version 1: 50.0% complete.
Updated to version 1: 100.0% complete.
OK: Schema has been set up.

BUILD SUCCESSFUL in 1s
...
```

If you run the ```gradle runSchemaSetup``` task again, you should see something like:

```
...
> Task :schema:runSchemaSetup
OK: Schema has been set up.
...
```

This indicates that it recognised the schema was up-to-date, and didn't need to perform any updates.

Later, we'll add to the schema definition to see how Wrapd automates schema migration. For now, we have a database with 
a table named *wrapd_myprojecttester01* with integer columns *x* and *y*.

## Step 7 - Create the Query Generator ##

The main purpose of Wrapd is to turn straightforward SQL query definitions into invocable Java methods. We'll make a copy of
the *schema* subproject for that purpose.

Simply copy everything in *schema* to a new subproject called *query*. Then:

1. Edit *settings.gradle* to reference the new *query* subproject. It should look like this:
   ```groovy
   rootProject.name = 'MyProject'
   include('app', 'schema', 'database', 'query')
   ```
2. Rename *query/main/java/org/reldb/myproject/schema* to *query/main/java/org/reldb/myproject/query*.
3. Delete any files in *query/main/java/org/reldb/myproject/query*. We're going to replace them.
4. Create a file called *Definitions.java* in *query/main/java/org/reldb/myproject/query* with the following content:
   ```java
   package org.reldb.myproject.query;
   
   import org.reldb.toolbox.utilities.Directory;
   import org.reldb.wrapd.sqldb.Database;
   import org.reldb.wrapd.sqldb.Definer;
   
   import org.reldb.myproject.database.GetDatabase;
   
   public class Definitions extends Definer {
   
       public Definitions(Database database, String codeDirectory, String packageSpec) {
           super(database, codeDirectory, packageSpec);
       }
   
       void generate() throws Throwable {
           purgeTarget();
   
           defineTable("$$tester01");
           defineQuery("SelectTester", "SELECT * FROM $$tester01 WHERE x = {xValue}", 1);
           defineUpdate("ClearTester", "DELETE FROM $$tester01");
   
           emitDatabaseAbstractionLayer("DatabaseAbstractionLayer");
       }
   
       public static void main(String[] args) throws Throwable {
           var db = GetDatabase.getDatabase();
           var codeDirectory = "../app/src/main/java";
           var codePackage = "org.reldb.myproject.app.generated";
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
5. Edit *query/gradle.build* to change this:
   ```groovy
   task runSchemaSetup(type: JavaExec) {
       group = "Wrapd"
       description "Ensure that the schema is up-to-date."
       classpath = sourceSets.main.runtimeClasspath
       mainClass = "org.reldb.myproject.schema.Schema"
   }
   ```
   ...to this, which will create a *runQueryGenerate* Gradle task to generate Java code from the SQL query definitions:
   ```groovy
   task runQueryGenerate(type: JavaExec) {
       group = "Wrapd"
       description "Generate database abstraction layer."
       classpath = sourceSets.main.runtimeClasspath
       mainClass = "org.reldb.myproject.query.Definitions"
   }
   ```
6. Run ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL.
7. Run ```gradle runQueryGenerate``` task to generate Java code. You should see output similar to the following:
   ```
   ...
   > Task :query:runQueryGenerate
   Target ../app/src/main/java/org/reldb/myproject/app/generated has been purged.
   OK: Queries are ready.
   ...
   ```

Now take a look in the *app/src/main/java/org/reldb/myproject/app/generated* directory to see the code generated by Wrapd and the runQueryGenerate task.

## Step 8 - Create the Application ##

In Step 7, we created a Query subproject that converts your query definitions into Java code to execute them.
We ran it to verify that it works.
The result is the generated code now available in your *app* subproject, waiting to be used. Let's use it.

1. In *app/build.gradle* replace *implementation 'org.apache.commons:commons-text'* with *implementation 'org.reldb:Wrapd:1.0.0'* to use Wrapd instead of Apache commons-text.
2. In *app/src/main/java/org/reldb/myproject/app* create a file called *App.java* with the following contents:
   ```java
   package org.reldb.myproject.app;
   
   import org.reldb.myproject.app.generated.*;
   import org.reldb.myproject.database.GetDatabase;
   
   public class App {
      public static void main(String args[]) throws Exception {
         var database = GetDatabase.getDatabase();
         var dbAbstraction = new DatabaseAbstractionLayer(database);

         // Clear table
         dbAbstraction.clearTester();
         // Populate table
         for (int x = 0; x < 100; x++) {
            var row = new Tester01Tuple(database);
            row.x = x;
            row.y = x * 10 + 2;
            row.insert();
         }
         // Show table
         dbAbstraction.tester01().forEach(System.out::println);
         // Show a row
         dbAbstraction.selectTester(2).forEach(System.out::println);
      }
   }
   ```
3. Run ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL.
4. Run ```gradle run``` to run the demonstration application. You should see it emit the query results.

This demonstrates the basic process for creating a Wrapd application. In the following steps, we'll handle schema migration and demonstrate more queries.

## Step 9 - Make a Schema Change ##

Now we'll make a schema change, to demonstrate how schema migration is handled.

Remember that we defined the initial database schema in *schema/src/main/java/org/reldb/myproject/schema/Schema* in Step 5 and Step 6. Now we'll modify it.

The schema is currently defined by:
```java
 @Override
 protected Update[] getUpdates() {
     return new Update[] {
         schema -> {
             getDatabase().updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
             return Result.OK;
         },
     };
 }
```

The getUpdates() method returns an array of Update, where each Update defines a schema migration. The first array entry defines the initial database schema. We add migrations by simply adding them to the array.

So, for example, if we want to add another table we can change the above to this:
```java
 @Override
 protected Update[] getUpdates() {
     return new Update[] {
         schema -> {
             getDatabase().updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
             return Result.OK;
         },
         schema -> {
             getDatabase().updateAll("CREATE TABLE $$tester02 (p VARCHAR(20) NOT NULL PRIMARY KEY, q FLOAT NOT NULL)");
             return Result.OK;
         },
     };
 }
```

Then run ```gradle clean build``` and verify that it emits BUILD SUCCESSFUL.

Now run ```gradle runSchemaSetup```. You should see the following output:
```
...
> Task :schema:runSchemaSetup
Updating to version 2: 0.0% complete.
Updated to version 2: 100.0% complete.
OK: Schema has been set up.

BUILD SUCCESSFUL in 1s
...
```

This shows that the schema has been successfully migrated. The current version is maintained within the database, so it will work on any connectable database to correctly migrating it from its current state or version (including empty) to the latest version.

Migration can either be invoked within the application (such as on every startup) to ensure that any database to which it connects is automatically migrated, or the migration can be deployed as a separate application to migrate databases outside the application.

Being able to revert migrations with specified regression steps will be a feature of a future Wrapd release.

Note that schema migrations must always -- and only -- be added as a new array entry to be returned by getUpdates(). Adding update queries to previous array entries or performing schema migrations outside of this mechanism will end in chaos. 

## Step 10 - More Queries ##

### ...to be continued... ###
