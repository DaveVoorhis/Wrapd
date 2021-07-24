package org.reldb.wrapd.tuples;

import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.JavaCompiler;
import org.reldb.wrapd.exceptions.ExceptionFatal;

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

    private final String dir;
    private String tupleName;
    private boolean existing;
    private long serialValue = 0;
    private final DirClassLoader loader;
    private String oldTupleName;
    private final List<Attribute> attributes = new LinkedList<>();
    private TupleTypeGenerator copyFrom = null;

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
            throw new ExceptionFatal(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
        loader = new DirClassLoader(dir, TupleTypePackage);
        try {
            var className = TupleTypePackage + "." + tupleName;
            var tupleClass = loader.forName(className);
            getDataFields(tupleClass).forEach(field -> attributes.add(new Attribute(field.getName(), field.getType())));
            try {
                var serialVersionUIDField = tupleClass.getDeclaredField("serialVersionUID");
                serialValue = serialVersionUIDField.getLong(null);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                serialValue = 9999999;
            }
            existing = true;
        } catch (ClassNotFoundException e) {
            existing = false;
        }
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
     * Return the ordinal position of an attribute with a given name.
     *
     * @param name Attribute name.
     * @return Ordinal position of attribute; -1 if not found.
     */
    public int positionOf(String name) {
        for (int index = 0; index < attributes.size(); index++) {
            if (attributes.get(index).name.equals(name))
                return index;
        }
        return -1;
    }

    /**
     * Return true if this tuple definition already exists.
     *
     * @return True if this tuple definition already exists, false if it is new.
     */
    public boolean isExisting() {
        return existing;
    }

    /**
     * Add the specified attribute of specified type. NOTE: Will not take effect until compile() has been invoked.
     *
     * @param name Name of new attribute.
     * @param type Type (class) of new attribute.
     */
    public void addAttribute(String name, Class<?> type) {
        if (typeOf(name) != null)
            throw new ExceptionFatal(Str.ing(ErrAttemptToAddDuplicateAttributeName, name, type.getName(), getTupleClassName()));
        attributes.add(new Attribute(name, type));
    }

    /**
     * Remove the specified attribute. NOTE: Will not take effect until compile() has been invoked.
     *
     * @param name Attribute to remove.
     */
    public void removeAttribute(String name) {
        int position = positionOf(name);
        if (position == -1)
            throw new ExceptionFatal(Str.ing(ErrAttemptToRemoveNonexistentAttribute, name));
        attributes.remove(position);
    }

    /**
     * Change attribute name.
     *
     * NOT IMPLEMENTED YET
     *
     * @param oldName The name of the attribute.
     * @param newName The neew name of the attribute.
     * @return Success or failure.
     */
    public Object renameAttribute(String oldName, String newName) {
        // TODO Auto-generated method stub
        System.out.println("TupleTypeGenerator: renameAttribute not implemented yet.");
        return null;
    }

    /**
     * Change attribute type.
     *
     * NOT IMPLEMENTED YET
     *
     * @param name The name of the attribute.
     * @param type The new type of the attribute.
     * @return Success or failure.
     */
    public Object changeAttributeType(String name, Class<?> type) {
        // TODO Auto-generated method stub
        System.out.println("TupleTypeGenerator: changeAttributeType not implemented yet.");
        return null;
    }

    /**
     * Rename this tuple type (class) definition and associated files to that specified by newName. NOTE: Will not take effect until compile() has been invoked.
     *
     * @param newName The new name, which must follow Java class name identifier rules.
     */
    public void rename(String newName) {
        if (existing && oldTupleName == null)
            oldTupleName = tupleName;
        tupleName = newName;
    }

    /**
     * Create a new tuple type (class) definition that is a copy of this one. NOTE: Will not take effect until compile() has been invoked.
     *
     * @param newName The new name, which must follow Java class name identifier rules.
     * @return A new TupleTypeGenerator which is a copy of this one.
     */
    public TupleTypeGenerator copyTo(String newName) {
        var target = new TupleTypeGenerator(dir, newName);
        target.attributes.addAll(attributes);
        target.serialValue = serialValue + 1;
        target.copyFrom = this;
        return target;
    }

    private String getCopyFromCode() {
        if (copyFrom == null)
            return "";
        return
                "\n\t/** Method to copy from specified tuple to this tuple.\n\t@param source - tuple to copy from */\n" +
                        "\tpublic void copyFrom(" + copyFrom.tupleName + " source) {\n" +
                        attributes.stream().filter(entry -> copyFrom.typeOf(entry.name) != null)
                                .map(entry -> "\t\tthis." + entry.name + " = source." + entry.name + ";\n").collect(Collectors.joining()) +
                        "\t}\n";
    }

    private String getFormatString() {
        return
                tupleName
                        + " {"
                        + attributes.stream().map(entry -> entry.name + " = %s").collect(Collectors.joining(", "))
                        + "}";
    }

    private String getContentString() {
        return
                attributes.stream().map(entry -> "this." + entry.name).collect(Collectors.joining(", "));
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
     * Delete this tuple type, given its name, before loading it.
     *
     * @param dir The directory containing this Tuple type.
     * @param className The class name of this Tuple type.
     * @return True if source code and class have been successfully deleted.
     */
    public static boolean destroy(String dir, String className) {
        var pathName = dir + File.separator + TupleTypePackage.replace('.', File.separatorChar) + File.separator + className;
        var fJava = new File(pathName + ".java");
        boolean fJavaDelete = fJava.delete();
        var fClass = new File(pathName + ".class");
        boolean fClassDelete = fClass.delete();
        return fJavaDelete && fClassDelete;
    }

    /**
     * Get the tuple serial number.
     *
     * @return Serial number.
     */
    public long getSerial() {
        return serialValue;
    }

    /**
     * Compile this tuple type as a class.
     *
     * @return An instance of ForeignCompilerJava.CompilationResults, which indicates compilation results.
     */
    public JavaCompiler.CompilationResults compile() {
        loader.unload(getTupleClassName());
        if (oldTupleName != null)
            oldTupleName = null;
        var version =
                "\n\t/** Version number */\n" +
                        "\tpublic static final long serialVersionUID = " + serialValue + ";\n";
        var attributeDefs =
                attributes
                        .stream()
                        .map(entry -> "\n\t/** Field */\n\tpublic " + entry.type.getCanonicalName() + " " + entry.name + ";\n")
                        .collect(Collectors.joining());
        var tupleDef =
                "package " + TupleTypePackage + ";\n\n" +
                        "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
                        "import org.reldb.wrapd.tuples.Tuple;\n\n" +
                        "/** " + tupleName + " tuple class version " + serialValue + " */\n" +
                        "public class " + tupleName + " extends Tuple {\n" +
                        version +
                        attributeDefs +
                        getCopyFromCode() +
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
        return getTupleClassName(tupleName);
    }

    /**
     * Return the fully-qualified class name of the tuple, given a new tuple.
     *
     * @param newName New (short) tuple class name.
     * @return Fully-qualified tuple class name.
     */
    public static String getTupleClassName(String newName) {
        return TupleTypePackage + "." + newName;
    }

}
