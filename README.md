Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator that helpfully exposes
SQL in Java rather than hiding it.

See https://wrapd.org for Wrapd documentation.

See https://github.com/DaveVoorhis/Wrapd-demo for a Wrapd-ly written demo application.

This is the source project for the Wrapd library.

### To build the Wrapd library (with javadoc and local publish) ###
```
gradle clean
gradle build
gradle javadoc
gradle publishToMavenLocal
```

### To test the Wrapd library ###

You need Docker installed on your system, because Docker images of popular DBMSs are used to perform end-to-end tests.
```
docker-compose up -d
gradle clean
gradle test
docker-compose down -v
```

### Documentation and examples are a work-in-progress. Wrapd will soon be available on Maven Central. Watch this space! ###
