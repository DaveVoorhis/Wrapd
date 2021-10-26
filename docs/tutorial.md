# Creating a Wrapd Project - With Gradle #

## Step 1 - Get the Tools ##

Make sure you have the required tools:

1. Java JDK 11+
2. Gradle 7.1+

## Step 2 - Start a Gradle Project ##

Create a directory to host your project. Change to it.

Type ```gradle init``` and press Enter.

Choose the "2: application" project type.

Choose the "3: Java" implementation language.

Choose "2: yes - application and library projects" in response to "Split functionality across multiple subprojects?"

Choose "1: Groovy" as the build script DSL.

Pick whatever project name you like.

For "Source package", choose a reasonable package name -- e.g., org.reldb.myproject

Your project skeleton will now be created. Take a look at what it created. You should see something like this:

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

As a "multiple subprojects" project, Gradle has created *utilities* and *list* as examples of library subprojects and *app* as the application subproject. We'll rename them shortly.

The *buildSrc* subproject, which often contains imperative custom build logic, can also contain settings common to the subprojects.

The *gradle* & *.gradle* directories and *gradlew* & *gradlew.bat* files -- along with *buildSrc -- provide Gradle build scripts and infrastructure.

The *settings.gradle* file defines top-level settings, such as what subprojects should be built.

The *.gitattributes* and *.gitignore* files contain handy defaults if you're using git to manage your source code.

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
3. Edit app/build.gradle and change "utilities" in the following to "database":
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

