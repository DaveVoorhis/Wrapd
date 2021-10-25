Wrapd
=====

Wrapd is a *SQL amplifier*, a lightweight database abstraction layer generator and schema migrator that helpfully exposes
SQL in Java rather than hiding it.

This is the source project for the Wrapd library. If you want to use Wrapd, read about it on [https://wrapd.org](https://wrapd.org) then go [Get Wrapd](#get-wrapd). If you want to improve Wrapd, start with [Extend Wrapd](#extend-wrapd).

### Use Wrapd ###

Wrapd documentation is at https://wrapd.org

Javadocs are at https://www.javadoc.io/doc/org.reldb/Wrapd

Get a Wrapd-ly written demo application from https://github.com/DaveVoorhis/Wrapd-demo

### Get Wrapd ###

Wrapd on Maven Central: [https://search.maven.org/artifact/org.reldb/Wrapd](https://search.maven.org/artifact/org.reldb/Wrapd)

Wrapd on GitHub: [https://github.com/DaveVoorhis/Wrapd](https://github.com/DaveVoorhis/Wrapd)

### Extend Wrapd ###

To extend, modify, customise or improve Wrapd, you'll need to download the source from GitHub and have the following tools on hand:

#### Requirements ####
- Docker. (Docker images of popular DBMSs are used to run end-to-end tests.)
- Java JDK 11+ 
- Gradle 7.1+
- Git

#### Build Wrapd ####

1. Check out Wrapd from GitHub. 
   ```git clone https://github.com/DaveVoorhis/Wrapd.git```
2. Change to the Wrapd directory.
3. Build Wrapd:
    ```
    docker-compose up -d
    sleep 10   # give DBMSs time to spool up
    gradle clean build publishToMavenLocal
    docker-compose down -v
    ```

Once you've done something good, please submit a pull request!

____
### Documentation and examples are a work-in-progress. ###
