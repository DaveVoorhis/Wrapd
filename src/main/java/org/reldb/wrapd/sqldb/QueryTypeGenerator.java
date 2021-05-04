package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.strings.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
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

    public static final String QueryTypePackage = "org.reldb.wrapd.tuples.generated";

    /**
     * Create a generator of compiled query invokers.
     *
     * @param dir       Directory into which generated class(es) will be put.
     * @param queryName Name of generated query class.
     * @param sqlText   SQL query text
     * @param args      Sample arguments
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
     */
    public static boolean destroy(String dir, String className) {
        var pathName = dir + File.separator + QueryTypePackage.replace('.', File.separatorChar) + File.separator + className;
        var fJava = new File(pathName + ".java");
        boolean fJavaDelete = fJava.delete();
        var fClass = new File(pathName + ".class");
        boolean fClassDelete = fClass.delete();
        return fJavaDelete && fClassDelete;
    }

    private String getParms() {
        String out = "";
        int pnum = 0;
        if (args != null && args.length > 0) {
            for (Object arg: args) {
                if (out.length() > 0)
                    out += ", ";
                out += arg.getClass().getCanonicalName() + " p" + pnum++;
            }
        }
        return out;
    }

    private String getArgs() {
        if (args == null || args.length == 0)
            return "null";
        String out = "";
        for (int pnum = 0; pnum < args.length; pnum++) {
            if (out.length() > 0)
                out += ", ";
            out += "p" + pnum;
        }
        return out;
    }

    private String getConstructor() {
        return
            "\tprotected " + queryName + "(String queryText, Class<? extends Tuple> tupleClass, Object... arguments) {\n" +
            "\t\tsuper(queryText, tupleClass, arguments);\n" +
            "\t}\n";
    }

    private String getGetMethod(String tupleTypeName) {
        return
            "\tpublic static Query<" + tupleTypeName + "> get(" + getParms() + ") {\n" +
            "\t\treturn new " + queryName + "(\"" + sqlText + "\", " + tupleTypeName + ".class, " + getArgs() + ");\n" +
            "\t}\n";
    }

    /**
     * Compile this query type as a class.
     *
     * @return - an instance of ForeignCompilerJava.CompilationResults, which indicates compilation results.
     */
    public ForeignCompilerJava.CompilationResults compile() {
        var tupleTypeName = queryName + "Tuple";
        var queryDef =
                "package " + QueryTypePackage + ";\n\n" +
                        "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
                        "import org.reldb.wrapd.tuples.Tuple;\n" +
                        "import org.reldb.wrapd.sqldb.Query;\n\n" +
                        "public class " + queryName + " extends Query {\n" +
                        getConstructor() +
                        "\n" +
                        getGetMethod(tupleTypeName) +
                        "}";
        var compiler = new ForeignCompilerJava(dir);
        return compiler.compileForeignCode(queryName, QueryTypePackage, queryDef);
    }

    /**
     * Return the class name of the tuple.
     *
     * @return - class name
     */
    public String getQueryClassName() {
        return getQueryClassName(queryName);
    }

    public static String getQueryClassName(String newName) {
        return QueryTypePackage + "." + newName;
    }

}
