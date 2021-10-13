package org.reldb.wrapd.sqldb;

/**
 * Generates Java code to ergonomically represent valueOf(...) queries.
 */
public class ValueOfTypeGenerator extends SQLTypeGenerator {
    private final String typeName;

    /**
     * Create a generator of compiled update invokers.
     *
     * @param codeDirectory Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param valueOfClassName Name of generated class.
     * @param type The type returned by valueOf*(...) in the generated class.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Sample arguments.
     */
    public ValueOfTypeGenerator(String codeDirectory, String packageSpec, String valueOfClassName, Class<?> type, String sqlText, Object... args) {
        super(codeDirectory, packageSpec, valueOfClassName, sqlText, args);
        this.typeName = type.getName();
    }

    private String buildQueryMethod(String methodName, String newQuery, boolean withConnection) {
        var argConnection = withConnection
            ? "connection, "
            : "";
        var newMethodName = "valueOf";
        var returnType = "Optional<" + typeName + ">";
        addMethod(new Method(methodName, "", getParameterList(true, withConnection), returnType));
        return
            "\tpublic static " + returnType + " " + newMethodName + "(" + getParameterDefinitionListString(withConnection) + ") throws SQLException {\n" +
            "\t\treturn (" + returnType + ")db." + methodName + "(" + argConnection + newQuery + ");\n" +
            "\t}\n";
    }

    private String getQueryMethods() {
        var methodName = hasArgs()
            ? "valueOf"
            : "valueOfAll";
        var args = hasArgs()
            ? ", " + getDeclaredQueryParameterNameListString()
            : "";
        var arguments = "sqlText" + args;
        return
            buildQueryMethod(methodName, arguments, false) +
            "\n" +
            buildQueryMethod(methodName, arguments, true);
    }

    @Override
    protected String getDefinitionSourceCode() {
        return
            "package " + getPackageSpec() + ";\n\n" +
            "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
            "import java.sql.SQLException;\n" +
            "import java.sql.Connection;\n" +
            "import java.util.Optional;\n" +
            "import org.reldb.wrapd.sqldb.Database;\n\n" +
            "public class " + getQueryName() + " {\n" +
            "\tprivate final static String sqlText = \"" + getSQLText() + "\";\n\n" +
            getQueryMethods() +
            "}";
    }
}
