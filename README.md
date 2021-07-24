Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer to make SQL easy.

Highly opinionated, Wrapd doesn't hide SQL from you. Instead, it's 
a pure Java, annotation-free *SQL amplifier* that makes SQL easier to use and better 
Java-integrated, tested, migrated and managed than using 
alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

*It's pronounced "wrapped", "rapid" and "rapt."*

* ResultSets are *wrapped* in Java Streams.
* Development is *rapid*.
* SQL focus is *rapt*.

### To build Wrapd: ###

1.   _gradle clean_
2.   _gradle publishToMavenLocal_

### To test Wrapd: ###

1.  _docker-compose up -d_
2.  _gradle clean_
3.  _gradle test_
4.  _docker-compose down -v_
