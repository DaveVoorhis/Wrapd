package org.reldb.wrapd.tuples;

import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.JavaCompiler;
import org.reldb.wrapd.exceptions.FatalException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reldb.wrapd.il8n.Strings.*;

/**
 * Generates Java code to represent a tuple, which is a class that implements Tuple.
 */
public class TupleTypeGenerator {
    private static class Attribute {
        final String name;
        final Class<?> type;
        Attribute(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
    }

    private final String dir;
    private String tupleName;
    private final DirClassLoader loader;
    private final List<Attribute> attributes = new LinkedList<>();

    /** The package name for generated Tuple types. */
    public static final String TupleTypePackage = "org.reldb.wrapd.tuples.generated";

    /**
     * Given a Class used as a tuple type, return a stream of fields suitable for data. Exclude static fields, metadata, etc.
     *
     * @param tupleClass Class&lt;?&gt; tuple type.
     * @return Stream&lt;Field&gt; of FieldS.
     */
    public static Stream<Field> getDataFields(Class<?> tupleClass) {
        return Arrays.stream(tupleClass.getFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()));
    }

    /**
     * Create a generator of compiled Tuple-derived classes, which can be used to conveniently receive SELECT query results and generate INSERT and UPDATE queries.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param tupleName Name of generated tuple class.
     */
    public TupleTypeGenerator(String dir, String tupleName) {
        if (tupleName.startsWith(TupleTypePackage))
            tupleName = tupleName.substring(TupleTypePackage.length() + 1);
        this.dir = dir;
        this.tupleName = tupleName;
        if (!Directory.chkmkdir(dir))
            throw new FatalException(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
        loader = new DirClassLoader(dir, TupleTypePackage);
    }

    /**
     * Delete this tuple type before loading it.
     *
     * @return True if source code and class have been successfully deleted.
     */
    public boolean destroy() {
        var pathName = dir + File.separator + TupleTypePackage.replace('.', File.separatorChar) + File.separator + tupleName;
        var fJava = new File(pathName + ".java");
        boolean fJavaDelete = fJava.delete();
        var fClass = new File(pathName + ".class");
        boolean fClassDelete = fClass.delete();
        return fJavaDelete && fClassDelete;
    }

    /**
     * Return the type of an attribute with a given name.
     *
     * @param name Attribute name.
     * @return Class&lt;?&gt; type of attribute or null if not found.
     */
    public Class<?> typeOf(String name) {
        for (var attribute : attributes)
            if (attribute.name.equals(name))
                return attribute.type;
        return null;
    }

    /**
     * Add the specified attribute of specified type. NOTE: Will not take effect until compile() has been invoked.
     *
     * @param name Name of new attribute.
     * @param type Type (class) of new attribute.
     */
    public void addAttribute(String name, Class<?> type) {
        if (typeOf(name) != null)
            throw new FatalException(Str.ing(ErrAttemptToAddDuplicateAttributeName, name, type.getName(), getTupleClassName()));
        attributes.add(new Attribute(name, type));
    }

    private String getFormatString() {
        return
                tupleName
                        + " {"
                        + attributes.stream().map(entry -> entry.name + " = %s").collect(Collectors.joining(", "))
                        + "}";
    }

    private String getContentString() {
        return attributes.stream().map(entry -> "this." + entry.name).collect(Collectors.joining(", "));
    }

    private String prefixWithCommaIfPresent(String s) {
        if (s != null && s.length() > 0)
            return ", " + s;
        return "";
    }

    private String getToStringCode() {
        return
                "\n\t/** Create string representation of this tuple. */\n" +
                "\tpublic String toString() {\n\t\treturn String.format(\"" + getFormatString() + "\"" + prefixWithCommaIfPresent(getContentString()) + ");\n\t}\n";
    }

    /**
     * Compile this tuple type as a class.
     *
     * @return Compilation results.
     */
    public JavaCompiler.CompilationResults compile() {
        loader.unload(getTupleClassName());
        var attributeDefs =
                attributes
                        .stream()
                        .map(entry -> "\n\t/** Field */\n\tpublic " + entry.type.getCanonicalName() + " " + entry.name + ";\n")
                        .collect(Collectors.joining());
        var version = "\tpublic static final long serialVersionUID = 0L;\n";
        var tupleDef =
                "package " + TupleTypePackage + ";\n\n" +
                "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
                "import org.reldb.wrapd.tuples.Tuple;\n\n" +
                "public class " + tupleName + " extends Tuple {\n" +
                    version +
                    attributeDefs +
                    getToStringCode() +
                "}";
        var compiler = new JavaCompiler(dir);
        return compiler.compileJavaCode(tupleName, TupleTypePackage, tupleDef);
    }

    /**
     * Return the class name of the tuple.
     *
     * @return Class name.
     */
    public String getTupleClassName() {
        return TupleTypePackage + "." + tupleName;
    }

}
