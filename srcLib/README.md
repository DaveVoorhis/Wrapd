These libraries are required but aren't available in the usual 
public repositories like Maven Central. Formerly, this project
used the Maven _clean_ phase to install them into
the local Maven repository. That's questionable, so now they're
kept here and referenced in the _build.gradle_ file.

You'll need to include them in your classpath.

```ecj-x.xx.jar``` is the Eclipse Compiler for Java from the Eclipse JDT Core.

```je-x.x.xx.jar``` is the Berkeley DB Java Edition.
