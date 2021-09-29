package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.generator.JavaGenerator;
import org.reldb.wrapd.exceptions.FatalException;

import java.io.File;

import static org.reldb.wrapd.il8n.Strings.*;

/**
 * Generates Java code to represent a query, which is a class that implements {@link Query}.
 */
public class QueryTypeGenerator {

    private final String dir;
    private final String packageSpec;
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    private String tableName;

    /**
     * Create a generator of compiled query invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text.
     * @param args Sample arguments.
     */
    public QueryTypeGenerator(String dir, String packageSpec, String queryName, String sqlText, Object[] args) {
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

    private String getConstructor(String tupleTypeName) {
        return
            "\t@SuppressWarnings(\"unchecked\")\n" +
            "\tprotected " + queryName + "(String queryText, Class<" + tupleTypeName + "> tupleClass, Object... arguments) {\n" +
            "\t\tsuper(queryText, (Class<T>)tupleClass, arguments);\n" +
            "\t}\n";
    }

    private String buildQueryMethod(String methodName, String tupleTypeName, String newQuery, boolean withConnection, String methodNameSuffix) {
        var argConnection = withConnection
                ? "connection, "
                : "";
        return "\tpublic static Stream<" + tupleTypeName + "> query" + methodNameSuffix + "(" + getParms(withConnection) + ") throws SQLException {\n" +
                "\t\treturn db." + methodName + "(" + argConnection + newQuery + ");\n" +
                "\t}\n";
    }

    private String getQueryMethodsSpecific(String tupleTypeName, String methodNameSuffix) {
        var methodName = (args == null || args.length == 0)
                ? ("queryAll" + methodNameSuffix)
                : ("query" + methodNameSuffix);
        var args = hasArgs()
                ? ", " + getArgs()
                : "";
        var newQuery = "new " + queryName + "<>(sqlText, " + tupleTypeName + ".class" + args + ")";
        return
                buildQueryMethod(methodName, tupleTypeName, newQuery, false, methodNameSuffix) +
                "\n" +
                buildQueryMethod(methodName, tupleTypeName, newQuery, true, methodNameSuffix);
    }

    private String getQueryMethods(String tupleTypeName) {
        return
                getQueryMethodsSpecific(tupleTypeName, "") +
                (tableName != null
                    ? "\n" + getQueryMethodsSpecific(tupleTypeName, "ForUpdate")
                    : "");
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
                "import java.util.stream.Stream;\n\n" +
                "import org.reldb.wrapd.sqldb.Tuple;\n" +
                "import org.reldb.wrapd.sqldb.Database;\n" +
                "import org.reldb.wrapd.sqldb.Query;\n\n" +
                "public class " + queryName + "<T extends Tuple> extends Query<T> {\n" +
                "\tprivate final static String sqlText = \"" + sqlText + "\";\n\n" +
                getConstructor(tupleTypeName) +
                "\n" +
                getQueryMethods(tupleTypeName) +
                "}";
        var generator = new JavaGenerator(dir);
        return generator.generateJavaCode(queryName, packageSpec, queryDef);
    }

    /**
     * Set table name.
     *
     * @param tableName If not null, methods will be created to update/insert to this table.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
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
