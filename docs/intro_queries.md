# SQL Queries are Easy #

Predefine SQL queries like this, which tests the queries and automatically generates the database access layer:
```java
public class MyQueryDefinitions extends QueryDefiner {
      ...
      public QueryDefinition QueryDefinition01() {
          return new QueryDefinition("MyTableQuery01", 
              "SELECT * FROM mytable WHERE x > ? AND x < ?", 3, 7);
      }
      ...
}
```
Note the example arguments, which are used to test the query and determine its parameter and result types.

Wrapd will generate classes and type-checked methods to conveniently invoke your queries. Run the query defined above like this:
```java
MyTableQuery01.query(database, 22, 88)
              .forEach(tuple -> System.out.println("x = " + tuple.x + ", y = " + tuple.y));
```

In short, Wrapd provides a type-checked, statically-compiled bridge between JDBC and Java Streams.

----
[Home](index.md)