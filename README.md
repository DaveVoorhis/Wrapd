Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator that helpfully exposes
SQL in Java rather than hiding it.

This is the source project for the Wrapd library. If you want to use Wrapd, go [Get Wrapd](#get-wrapd). If you want to improve Wrapd, start with [build Wrapd](#build-wrapd).

### Use Wrapd ###

Wrapd document is at https://wrapd.org

Get a Wrapd-ly written demo application at https://github.com/DaveVoorhis/Wrapd-demo

### Get Wrapd ###

Wrapd on Maven Central: [https://search.maven.org/artifact/org.reldb/Wrapd](https://search.maven.org/artifact/org.reldb/Wrapd)

Wrapd on GitHub: [https://github.com/DaveVoorhis/Wrapd](https://github.com/DaveVoorhis/Wrapd)

### Build Wrapd ###

You need Docker installed on your system, because Docker images of popular DBMSs are used to perform end-to-end tests.

This will build, test, and locally publish Wrapd:

```
docker-compose up -d
sleep 10   # give DBMSs time to spool up
gradle clean
gradle build
gradle javadoc
gradle publishToMavenLocal
docker-compose down -v
```

____
### Documentation and examples are a work-in-progress. ###
