Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator that helpfully exposes
SQL in Java rather than hiding it.

See https://wrapd.org for Wrapd documentation.

See https://github.com/DaveVoorhis/Wrapd-demo for a Wrapd-ly written demo application.

This is the source project for the Wrapd library.

### To build and test the Wrapd library (with javadoc and local publish) ###

You need Docker installed on your system, because Docker images of popular DBMSs are used to perform end-to-end tests.

```
docker-compose up -d
sleep 10   # give DBMSs time to spool up
gradle clean
gradle build
gradle javadoc
gradle publishToMavenLocal
docker-compose down -v
```

Note: If building locally, you need the RelDB.org Toolbox dependency in your
local Maven repository. See https://github.com/DaveVoorhis/Toolbox

### Documentation and examples are a work-in-progress. Available on Maven Central. See https://search.maven.org/artifact/org.reldb/Wrapd ###
