package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.generator.JavaGenerator;

import java.io.File;

import static org.reldb.wrapd.il8n.Strings.ErrUnableToCreateOrOpenCodeDirectory;

/**
 * Generates Java code to represent an update query, which is a class that implements {@link Update}.
 */
public class UpdateTypeGenerator {

    private final String dir;
    private final String packageSpec;
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    /**
     * Create a generator of compiled update invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text.
     * @param args Sample arguments.
     */
    public UpdateTypeGenerator(String dir, String packageSpec, String queryName, String sqlText, Object[] args) {
        this.packageSpec = packageSpec;
        if (queryName.startsWith(packageSpec))
            queryName = queryName.substring(packageSpec.length() + 1);
        this.dir = dir;
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
        if (!Directory.chkmkdir(dir))
            throw new FatalException(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
    }

    /**
     * Delete this query type, given its name, before loading it.
     *
     * @param dir The directory containing the query definition.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param className The class name of the query definition.
     * @return boolean true if query definition successfully deleted.
     */
    public static boolean destroy(String dir, String packageSpec, String className) {
        var pathName = dir + File.separator + packageSpec.replace('.', File.separatorChar) + File.separator + className;
        var fJava = new File(pathName + ".java");
        var fJavaDelete = fJava.delete();
        var fClass = new File(pathName + ".class");
        var fClassDelete = fClass.delete();
        return fJavaDelete && fClassDelete;
    }

    private boolean hasArgs() {
        return args != null && args.length > 0;
    }

    private String getParms(boolean withConnection) {
        String parmConnection = withConnection
                ? ", Connection connection"
                : "";
        StringBuilder out = new StringBuilder("Database db" + parmConnection);
        int parameterNumber = 0;
        if (hasArgs()) {
            for (Object arg: args)
                out.append(", ").append(arg.getClass().getCanonicalName()).append(" p").append(parameterNumber++);
        }
        return out.toString();
    }

    private String getArgs() {
        if (!hasArgs())
            return "null";
        var out = new StringBuilder();
        for (var parameterNumber = 0; parameterNumber < args.length; parameterNumber++) {
            if (out.length() > 0)
                out.append(", ");
            out.append("p").append(parameterNumber);
        }
        return out.toString();
    }

    private String getConstructor() {
        return
            "\t@SuppressWarnings(\"unchecked\")\n" +
            "\tprotected " + queryName + "(String queryText" +
                (hasArgs() ? ", Object... arguments" : "") +
            ") {\n" +
            "\t\tsuper(queryText, " +
                (hasArgs() ? "arguments" : "(Object)null") +
            ");\n" +
            "\t}\n";
    }

    private String buildQueryMethod(String methodName, String newQuery, boolean withConnection) {
        var argConnection = withConnection
                ? "connection, "
                : "";
        return "\tpublic static boolean update(" + getParms(withConnection) + ") throws SQLException {\n" +
                "\t\tfinal String sqlText = \"" + sqlText + "\";\n" +
                "\t\treturn db." + methodName + "(" + argConnection + newQuery + ");\n" +
                "\t}\n";
    }

    private String getQueryMethods() {
        var methodName = (args == null || args.length == 0)
                ? "updateAll"
                : "update";
        var args = hasArgs()
                ? ", " + getArgs()
                : "";
        var newQuery = "new " + queryName + "(sqlText" + args + ")";
        return
                buildQueryMethod(methodName, newQuery, false) +
                "\n" +
                buildQueryMethod(methodName, newQuery, true);
    }

    /**
     * Generate this query type as a Java class definition.
     *
     * @return The generated Java source file.
     */
    public File generate() {
        var tupleTypeName = queryName + "Tuple";
        var queryDef =
                "package " + packageSpec + ";\n\n" +
                "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
                "import java.sql.SQLException;\n" +
                "import java.sql.Connection;\n" +
                "import org.reldb.wrapd.sqldb.Database;\n" +
                "import org.reldb.wrapd.sqldb.Update;\n\n" +
                "public class " + queryName + " extends Update {\n" +
                getConstructor() +
                "\n" +
                getQueryMethods() +
                "}";
        var generator = new JavaGenerator(dir);
        return generator.generateJavaCode(queryName, packageSpec, queryDef);
    }

    /**
     * Return the class name.
     *
     * @return The class name.
     */
    public String getQueryClassName() {
        return getQueryClassName(packageSpec, queryName);
    }

    /**
     * Given a class name, return a fully-qualified class name obtained by prepending packageSpec.
     *
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param newName The query class name.
     * @return Fully-qualified class name.
     */
    public static String getQueryClassName(String packageSpec, String newName) {
        return packageSpec + "." + newName;
    }

}