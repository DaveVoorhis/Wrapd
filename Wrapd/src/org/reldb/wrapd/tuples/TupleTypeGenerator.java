package org.reldb.wrapd.tuples;

import static org.reldb.wrapd.strings.Strings.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.strings.Str;
import org.reldb.wrapd.utilities.Directory;

/**
 * Generates Java code to represent a tuple, which is a class that implements Tuple.
 * 
 * @author dave
 *
 */
public class TupleTypeGenerator {
	
	private String dir;
	private String tupleName;
	private boolean existing;
	private long serialValue = 0;
	private DirClassLoader loader;
	private String oldTupleName;	
	private LinkedList<Attribute> attributes = new LinkedList<>();
	private TupleTypeGenerator copyFrom = null;

	public static final String tupleTypePackage = "org.reldb.wrapd.tuples.generated";
	
	/**
	 * Given a Class used as a tuple type, return a stream of fields suitable for data. Exclude static fields, metadata, etc.
	 * 
	 * @param tupleClass - Class<?> - tuple type
	 * @return Stream<Field> of FieldS.
	 */
	public static Stream<Field> getDataFields(Class<?> tupleClass) {
		return Arrays.stream(tupleClass.getFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()));
	}
	
	public TupleTypeGenerator(String dir, String tupleName) {
		if (tupleName.startsWith(tupleTypePackage))
			tupleName = tupleName.substring(tupleTypePackage.length() + 1);
		this.dir = dir;
		this.tupleName = tupleName;
		if (!Directory.chkmkdir(dir))
			throw new ExceptionFatal(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
		loader = new DirClassLoader(dir, tupleTypePackage);
		try {
			var className = tupleTypePackage + "." + tupleName;
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
	 * @param name - String - attribute name
	 * @return - Class<?> - type of attribute or null if not found.
	 */
	public Class<?> typeOf(String name) {
		for (var attribute: attributes)
			if (attribute.name.equals(name))
				return attribute.type;
		return null;
	}
	
	/**
	 * Return the ordinal position of an attribute with a given name.
	 * 
	 * @param name - String - attribute name
	 * @return - int - ordinal position of attribute; -1 if not found.
	 */
	public int positionOf(String name) {
		for (int index = 0; index < attributes.size(); index++) {
			if (attributes.get(index).name.equals(name))
				return index;
		}
		return -1;
	}
	
	/** Return true if this tuple definition already exists.
	 * 
	 * @return - true if this tuple definition already exists, false if it is new.
	 */
	public boolean isExisting() {
		return existing;
	}
	
	/** Add the specified attribute of specified type. NOTE: Will not take effect until compile() has been invoked.
	 * 
	 * @param name - name of new attribute
	 * @param type - type (class) of new attribute
	 */
	public void addAttribute(String name, Class<?> type) {
		if (typeOf(name) != null)
			throw new ExceptionFatal(Str.ing(ErrAttemptToAddDuplicateAttributeName, name));			
		attributes.add(new Attribute(name, type));
	}
	
	/** Remove the specified attribute. NOTE: Will not take effect until compile() has been invoked.
	 * 
	 * @param name - attribute to remove
	 */
	public void removeAttribute(String name) {
		int position = positionOf(name);
		if (position == -1)
			throw new ExceptionFatal(Str.ing(ErrAttemptToRemoveNonexistentAttribute, name));
		attributes.remove(position);
	}

	public Object renameAttribute(String oldName, String newName) {
		// TODO Auto-generated method stub
		System.out.println("TupleTypeGenerator: renameAttribute not implemented yet.");
		return null;
	}

	public Object changeAttributeType(String name, Class<?> type) {
		// TODO Auto-generated method stub
		System.out.println("TupleTypeGenerator: changeAttributeType not implemented yet.");
		return null;
	}
	
	/** Rename this tuple type (class) definition and associated files to that specified by newName. NOTE: Will not take effect until compile() has been invoked.
	 * 
	 * @param newName - the new name, which must follow Java class name identifier rules.
	 */
	public void rename(String newName) {
		if (existing && oldTupleName == null)
			oldTupleName = tupleName;
		tupleName = newName;
	}
	
	/** Create a new tuple type (class) definition that is a copy of this one. NOTE: Will not take effect until compile() has been invoked.
	 * 
	 * @param newName - the new name, which must follow Java class name identifier rules.
	 * @return - a new TupleTypeGenerator which is a copy of this one.
	 */
	@SuppressWarnings("unchecked")
	public TupleTypeGenerator copyTo(String newName) {
		var target = new TupleTypeGenerator(dir, newName);
		target.attributes = (LinkedList<Attribute>)attributes.clone();
		target.serialValue = serialValue + 1;
		target.copyFrom = this;
		return target;
	}
	
	private String getCopyFromCode() {
		if (copyFrom == null)
			return "";
		return
			"\t/** Method to copy from specified tuple to this tuple.\n\t@param source - tuple to copy from */\n" +
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

	private String prefixIfPresent(String s, String prefix) {
		if (s != null && s.length() > 0)
			return prefix + s;
		return "";
	}
	
	private String getToStringCode() {
		return
			"\t/** Create string representation of this tuple. */\n" +
			"\tpublic String toString() {\n\t\treturn String.format(\"" + getFormatString() + "\"" + prefixIfPresent(getContentString(), ", ") + ");\n\t}\n";
	}
	
	/** Delete this tuple type, given its name, before loading it. */
	public static void destroy(String dir, String className) {
		var pathName = dir + File.separator + tupleTypePackage.replace('.', File.separatorChar) + File.separator + className;
		var fJava = new File(pathName + ".java");
		fJava.delete();
		var fClass = new File(pathName + ".class");
		fClass.delete();
	}

	public long getSerial() {
		return serialValue;
	}
	
	/** Compile this tuple type as a class.
	 * 
	 * @return - an instance of ForeignCompilerJava.CompilationResults, which indicates compilation results.
	 */
	public ForeignCompilerJava.CompilationResults compile() {
		loader.unload(getTupleClassName());
		if (oldTupleName != null)
			oldTupleName = null;
		var tupleDef = 
			"package " + tupleTypePackage + ";\n\n" +
			"/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
			"import org.reldb.wrapd.tuples.Tuple;\n\n" +
			"/** " + tupleName + " tuple class version " + serialValue + " */\n" +
			"public class " + tupleName + " implements Tuple {\n" +
				"\t/** Version number */\n" +
				"\tpublic static final long serialVersionUID = " + serialValue + ";\n" +
				attributes
					.stream()
					.map(entry -> "\t/** Field */\n\tpublic " + entry.type.getCanonicalName() + " " + entry.name + ";\n")
					.collect(Collectors.joining()) +
				getCopyFromCode() +
				getToStringCode() +
			"}";
		var compiler = new ForeignCompilerJava(dir);
		return compiler.compileForeignCode(tupleName, tupleTypePackage, tupleDef);
	}

	/**
	 * Return the class name of the tuple.
	 * 
	 * @return - class name
	 */
	public String getTupleClassName() {
		return getTupleClassName(tupleName);
	}

	public static String getTupleClassName(String newName) {
		return tupleTypePackage + "." + newName;
	}

	public static String getTuplePackage() {
		return tupleTypePackage;
	}
	
}
