Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator that helpfully exposes
SQL in Java rather than hiding it.

Highly opinionated, Wrapd doesn't hide SQL from you. Instead, it makes SQL easier 
to use and better integrated, tested, migrated and managed than using 
alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

See https://davevoorhis.github.io/Wrapd/ for Wrapd documentation and rationale.

This is the source project for the Wrapd library.

### To build the Wrapd library ###

1.   _gradle clean_
2.   _gradle publishToMavenLocal_

### To test the Wrapd library ###

You need Docker installed on your system, because Docker images of popular DBMSs are used to perform end-to-end tests.

1.  _docker-compose up -d_
2.  _gradle clean_
3.  _gradle test_
4.  _docker-compose down -v_
 
### Documentation and examples are a work-in-progress. Wrapd will soon be available on Maven Central. Watch this space! ###
