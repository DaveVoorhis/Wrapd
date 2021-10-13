package org.reldb.wrapd.sqldb;

/**
 * Generates Java code to represent an update query, which is a class that implements {@link Update}.
 */
public class UpdateTypeGenerator extends SQLTypeGenerator {

    /**
     * Create a generator of compiled update invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Sample arguments.
     */
    public UpdateTypeGenerator(String dir, String packageSpec, String queryName, String sqlText, Object... args) {
        super(dir, packageSpec, queryName, sqlText, args);
    }

    private String getConstructor() {
        return
            "\t@SuppressWarnings(\"unchecked\")\n" +
            "\tprotected " + getQueryName() + "(String queryText" +
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
        var newMethodName = "update";
        var returnType = "boolean";
        addMethod(new Method(newMethodName, "", getParameterList(true, withConnection), returnType));
        return "\tpublic static " + returnType + " " + newMethodName + "(" + getParameterDefinitionListString(withConnection) + ") throws SQLException {\n" +
                "\t\treturn db." + methodName + "(" + argConnection + newQuery + ");\n" +
                "\t}\n";
    }

    private String getQueryMethods() {
        var methodName = hasArgs()
                ? "update"
                : "updateAll";
        var args = hasArgs()
                ? ", " + getDeclaredQueryParameterNameListString()
                : "";
        var newQuery = "new " + getQueryName() + "(sqlText" + args + ")";
        return
                buildQueryMethod(methodName, newQuery, false) +
                "\n" +
                buildQueryMethod(methodName, newQuery, true);
    }

    @Override
    protected String getDefinitionSourceCode() {
        return
            "package " + getPackageSpec() + ";\n\n" +
            "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
            "import java.sql.SQLException;\n" +
            "import java.sql.Connection;\n" +
            "import org.reldb.wrapd.sqldb.Database;\n" +
            "import org.reldb.wrapd.sqldb.Update;\n\n" +
            "public class " + getQueryName() + " extends Update {\n" +
            "\tprivate final static String sqlText = \"" + getSQLText() + "\";\n\n" +
            getConstructor() +
            "\n" +
            getQueryMethods() +
            "}";
    }

}
