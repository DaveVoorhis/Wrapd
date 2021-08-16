# Rationale

Wrapd is a bridge between SQL and Java, particularly between JDBC and Java Streams, to reduce the pain of the usual SQL-in-Java pain points. That includes avoiding hulking, complicated, awkward ORMs.

There is intentionally no attempt to hide SQL. Instead, Wrapd exposes it appropriately -- but preferably only in the database abstraction layer -- and makes it easy to use SQL query results with Java Streams.

That -- plus automated schema migration -- is Wrapd's main selling point: JDBC-to-Streams, bridged.

It's easy:

1. Create your database. Wrapd is a "SQL first" library, where the presumption is that the database exists, either via Wrapd's schema migration mechanisms or externally defined.

2. Define your SQL queries inside Java. Collectively, your query definitions form a database abstraction layer. All the SQL lives within the database abstraction layer. Normally, no SQL is found outside it, because all queries will be invoked by regular type-safe Java methods.

3. Run some simple machinery (not shown here) to iterate all the query definitions and generate Java source code from them and compile the source code to binary .class files.

4. Use the generated methods to invoke your previously-defined SQL queries with type-checked parameters and no visible SQL (that's in the query definitions, in the database abstraction layer) which emit Java Streams with native, statically type-checked attributes.

That's SQL, amplified, in Java.

----
[Home](index.md)