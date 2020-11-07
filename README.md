Wrapd
=====

Wrapd is a lightweight, pure Java, XML-and-annotation-free data 
abstraction layer. 

It's pronounced "wrapped", "rapid" and "rapt."

Highly opinionated, Wrapd doesn't hide SQL from you. Instead, it's 
a *SQL amplifier* that makes SQL easier to use and better 
Java- integrated, tested, migrated and managed than using 
alternatives like ORMs (Object-Relational Mappers) or raw JDBC,
while staying light, lean, and loveable.

Wrapd abstracts other data sources too -- like Berkeley DBs, CSV
files and Excel spreadsheets -- under a unified row-and-column
relational-model-inspired paradigm designed to work 
effectively with Java Streams.

### To build Wrapd: ###

1.   _gradle clean_
2.   _gradle publishToMavenLocal_

### To test Wrapd: ###

1.   _gradle clean_
2.   _gradle test_
