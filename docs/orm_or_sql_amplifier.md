# ORM or SQL Amplifier?

There is unquestionably a sweet spot where ORMs work well, which is great.

But there are numerous cases where they don’t:

- Where the requirements emphasise writing custom queries.
- Where performance optimisation is needed beyond what the ORM provides (by default), 
  including low application load times (e.g., for microservices).
- Where performance contradictions are introduced by the ORM; e.g., we need lazy 
  loading over _here_ but eager loading of the same thing over _there_.
- Where seamless, type-checked integration with Streams (Java) is desirable and the ORM 
  doesn’t support it or makes it difficult.
- Where the requirements are very lightweight, such that an ORM is overkill.
- Where the requirements are very DBMS and/or query bound, such that an ORM is an obstacle.
- Where the DBMS is unlikely to change during the product lifetime. Changing the DBMS without 
  rewriting the client is rare. If the DBMS changes, there’s usually a client-side 
  from-scratch rewrite to go with it.
- Where we need to bolt application functionality to existing database infrastructure 
  and follow its schema changes rather than generating them.

Dependence on a particular DBMS is sometimes raised as a limitation of Wrapd and similar 
tools, but the DBMS rarely changes during the product lifetime (as noted above) and 
changing SQL dialects is relatively easy, particularly as Wrapd tests all queries.

Notably, an ORM typically trades a relatively light SQL-dialect dependence for a 
heavy ORM dependence. Is that really an improvement?

In Wrapd, the build tools are built into the Wrapd library and are trivially and largely 
invisibly integrated into (for example) Gradle build pipelines. Yes, it benefits from a 
particular build strategy and project structure, but that’s conceptually no different from 
typical ORM-based projects and arguably simpler than the configuration files and/or 
annotations required by some ORMs.

That said, Wrapd and similar SQL amplifier tools aren't a panacea or 
a silver bullet — there is no such thing — but for use cases that would either 
work against ORMs or that would struggle with raw ODBC/JDBC, Wrapd and similar
SQL amplifiers are a potent alternative.
