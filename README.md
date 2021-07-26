Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer to make using SQL in Java easy
by exposing it the right way rather than hiding it.

Highly opinionated, Wrapd doesn't hide SQL from you. Instead, it makes SQL easier 
to use and better integrated, tested, migrated and managed than using 
alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

*"Wrapd" is pronounced "wrapped", "rapid" and "rapt."*

* SQL access is helpfully *wrapped*, not awkwardly hidden.
* SQL-in-Java development is *rapid*.
* SQL focus can be *rapt*.

### Key Features ###

1. **SQL Queries are Easy.**<br>
   You can use Java Streams on query results and reference columns as native attributes. You can do this:
    ```
    database.query("SELECT * FROM mytable WHERE x > ? AND x < ?", MyTable.class, 3, 7)
            .forEach(tuple -> System.out.println("x = " + tuple.x + ", y = " + tuple.y));
   ```
   The required class, MyTable, is generated for you.

   You can define parametric SQL queries with example arguments like this, which
   also serve as tests:
   ```
    public class QueryDefinitions extends QueryDefiner {
      ...
      public QueryDefinition QueryDefinition01() {
          return new QueryDefinition("QueryDefinition01", 
              "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
      }
      ...
    }
    ```
   Wrapd will generate convenient, type-checked methods to invoke your queries. You can use them like this:
   ```
   QueryDefinition01.query(database, 3, 7)
            .forEach(tuple -> System.out.println("x = " + tuple.x + ", y = " + tuple.y));
   ```

2. **SQL Schemas are Easy.**<br>
   Version migrations/upgrades can be completely automated.

### To build Wrapd ###

1.   _gradle clean_
2.   _gradle publishToMavenLocal_

### To test Wrapd ###

1.  _docker-compose up -d_
2.  _gradle clean_
3.  _gradle test_
4.  _docker-compose down -v_
 
### Documentation and examples are a work-in-progress. Watch this space! ###
