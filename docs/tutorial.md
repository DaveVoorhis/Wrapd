Creating a Wrapd Project - With Gradle
======================================

## Step 1 - Get the Tools ##

Make sure you have the required tools for this tutorial:

1. Java JDK 11+
2. Gradle 7.1+
3. MySQL 5+.
   A Docker container running a MySQL instance is ideal; see the *docker-compose.yml* file in the [Wrapd demo](https://github.com/DaveVoorhis/Wrapd-demo) project. Wrapd can work with any JDBC-compatible SQL DBMS and its test suite is configured for at least MySQL, PostgreSQL and SQLite. For tutorial purposes, we'll use MySQL here.

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

Some things to note:
- As a "multiple subprojects" project, Gradle has created *utilities* and *list* as examples of library subprojects and *app* as the application subproject. We'll rename them shortly. 
- The *buildSrc* subproject, which often contains imperative custom build logic, can also contain settings common to some of or all the other subprojects. 
- The *gradle* & *.gradle* directories and *gradlew* & *gradlew.bat* files -- along with *buildSrc* -- provide Gradle build scripts and infrastructure. 
- The *settings.gradle* file defines top-level settings, such as what subprojects should be built. 
- The *.gitattributes* and *.gitignore* files contain handy defaults if you're using git to manage your source code.

## Step 3 - Create a Database Subproject ##

Connecting to a database, even an empty one, is fundamental to Wrapd's way of working. 
You need to start with a (possibly empty) database!

For development purposes, you may wish to launch a Docker container that hosts your
target DBMS. The [Wrapd demo](https://github.com/DaveVoorhis/Wrapd-demo) project provides a 
good illustration of this, along with a sample docker-compose.yml file to launch an appropriate container.

First, let's turn the *utilities* subproject into a *database* subproject. It will provide connectivity
to a SQL DBMS, and will be used by the other subprojects.

1. Rename *utilities* to *database*. This renames the subproject directory.
2. Edit *settings.gradle* and change *utilities* to *database*. This tells Gradle to build the *database* subproject.
3. Rename *database/src/main/java/org/reldb/myproject/utilities* to *database/src/main/java/org/reldb/myproject/database*. This renames the formerly-*utilities* directory to a more appropriate *database*.
4. Delete the sample auto-generated files in *database/src/main/java/org/reldb/myproject/database*.
5. Edit app/build.gradle and change "utilities" in the following to "database":
   ```
   dependencies {
       implementation 'org.apache.commons:commons-text'
       implementation project(':utilities')
   }
   ```
   It should now be:
   ```
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
   
   These values work with the Docker MySQL database defined in the docker-compose.yml from [Wrapd demo](https://github.com/DaveVoorhis/Wrapd-demo). If you're using your own MySQL DBMS, change as appropriate. If you're using a MySQL DBMS other than the Docker instance, you may need to create the database beforehand.

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

6. Run ```gradle clean build``` to verify that the build works so far. You should see BUILD SUCCESSFUL. This not only verifies that the project builds, it verifies database connectivity too.

## Step 5 - Create a Schema Subproject ##
