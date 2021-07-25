Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer to make SQL in Java easy by not hiding it.

Highly opinionated, Wrapd doesn't hide SQL from you. Instead, it's 
a pure Java, annotation-free *SQL amplifier* that makes SQL easier to use and better 
Java-integrated, tested, migrated and managed than using 
alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

Wrapd's key features are:

1. **SQL Queries are Easy.**<br>
   You can use Java Streams on query results and reference columns as native attributes. You can do this:
    ```
    database.query("SELECT * FROM mytable WHERE x > ? AND x < ?", MyTable.class, 3, 7)
            .forEach(tuple -> System.out.println("x = " + tuple.x + ", y = " + tuple.y));
   ```

2. **SQL Schemas are Easy.**<br>
   Version migrations/upgrades can be completely automated.

*"Wrapd" is pronounced "wrapped", "rapid" and "rapt."*

* SQL access is helpfully *wrapped*, not awkwardly hidden.
* SQL in Java development is *rapid*.
* SQL focus can be *rapt*.

### To build Wrapd: ###

1.   _gradle clean_
2.   _gradle publishToMavenLocal_

### To test Wrapd: ###

1.  _docker-compose up -d_
2.  _gradle clean_
3.  _gradle test_
4.  _docker-compose down -v_
 
### Documentation and examples are a work-in-progress. Watch this space! ###
