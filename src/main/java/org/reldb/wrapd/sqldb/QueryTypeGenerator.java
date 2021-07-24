package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.JavaCompiler;
import org.reldb.wrapd.exceptions.ExceptionFatal;

import java.io.File;

import static org.reldb.wrapd.il8n.Strings.*;

/**
 * Generates Java code to represent a query, which is a class that implements Query.
 */
public class QueryTypeGenerator {

    private final String dir;
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    /** Package to which generated query code belongs. */
    public static final String QueryTypePackage = "org.reldb.wrapd.tuples.generated";

    /**
     * Create a generator of compiled query invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text.
     * @param args Sample arguments.
     */
    public QueryTypeGenerator(String dir, String queryName, String sqlText, Object[] args) {
        if (queryName.startsWith(QueryTypePackage))
            queryName = queryName.substring(QueryTypePackage.length() + 1);
        this.dir = dir;
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
        if (!Directory.chkmkdir(dir))
            throw new ExceptionFatal(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
    }

    /**
     * Delete this query type, given its name, before loading it.
     *
     * @param dir The directory containing the query definition.
     * @param className The class name of the query definition.
     * @return boolean true if query definition successfully deleted.
     */
    public static boolean destroy(String dir, String className) {
        var pathName = dir + File.separator + QueryTypePackage.replace('.', File.separatorChar) + File.separator + className;
        var fJava = new File(pathName + ".java");
        boolean fJavaDelete = fJava.delete();
        var fClass = new File(pathName + ".class");
        boolean fClassDelete = fClass.delete();
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
        int pnum = 0;
        if (hasArgs()) {
            for (Object arg: args)
                out.append(", ").append(arg.getClass().getCanonicalName()).append(" p").append(pnum++);
        }
        return out.toString();
    }

    private String getArgs() {
        if (!hasArgs())
            return "null";
        StringBuilder out = new StringBuilder();
        for (int pnum = 0; pnum < args.length; pnum++) {
            if (out.length() > 0)
                out.append(", ");
            out.append("p").append(pnum);
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

    private String buildQueryMethod(String methodName, String tupleTypeName, String newQuery, boolean withConnection) {
        String argConnection = withConnection
                ? "connection, "
                : "";
        return "\tpublic static Stream<" + tupleTypeName + "> query(" + getParms(withConnection) + ") throws SQLException {\n" +
                "\t\tfinal String sqlText = \"" + sqlText + "\";\n" +
                "\t\treturn db." + methodName + "(" + argConnection + newQuery + ");\n" +
                "\t}\n";
    }

    private String getQueryMethods(String tupleTypeName) {
        String methodName = (args == null || args.length == 0)
                ? "queryAll"
                : "query";
        String args = hasArgs()
                ? ", " + getArgs()
                : "";
        String newQuery = "new " + queryName + "<>(sqlText, " + tupleTypeName + ".class" + args + ")";
        return
                buildQueryMethod(methodName, tupleTypeName, newQuery, false) +
                "\n" +
                buildQueryMethod(methodName, tupleTypeName, newQuery, true);
    }

    /**
     * Compile this query type as a class.
     *
     * @return An instance of ForeignCompilerJava.CompilationResults, which indicates compilation results.
     */
    public JavaCompiler.CompilationResults compile() {
        var tupleTypeName = queryName + "Tuple";
        var queryDef =
                "package " + QueryTypePackage + ";\n\n" +
                "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
                "import java.sql.SQLException;\n" +
                "import java.sql.Connection;\n" +
                "import java.util.stream.Stream;\n\n" +
                "import org.reldb.wrapd.tuples.Tuple;\n" +
                "import org.reldb.wrapd.sqldb.Database;\n" +
                "import org.reldb.wrapd.sqldb.Query;\n\n" +
                "public class " + queryName + "<T extends Tuple> extends Query<T> {\n" +
                getConstructor(tupleTypeName) +
                "\n" +
                getQueryMethods(tupleTypeName) +
                "}";
        var compiler = new JavaCompiler(dir);
        return compiler.compileJavaCode(queryName, QueryTypePackage, queryDef);
    }

    /**
     * Return the class name of the tuple.
     *
     * @return The class name.
     */
    public String getQueryClassName() {
        return getQueryClassName(queryName);
    }

    /**
     * Given a class name, return a fully-qualified class name obtained by prepending QueryTypePackage.
     *
     * @param newName The query class name.
     * @return Fully-qualified class name.
     */
    public static String getQueryClassName(String newName) {
        return QueryTypePackage + "." + newName;
    }

}
