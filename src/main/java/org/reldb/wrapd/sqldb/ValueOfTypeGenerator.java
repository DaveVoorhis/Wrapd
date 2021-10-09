package org.reldb.wrapd.sqldb;

public class ValueOfTypeGenerator extends SQLTypeGenerator {
    private final Class<?> type;

    public ValueOfTypeGenerator(String codeDirectory, String packageSpec, String valueOfClassName, Class<?> type, String sqlText, Object... args) {
        super(codeDirectory, packageSpec, valueOfClassName, sqlText, args);
        this.type = type;
    }

    private String buildQueryMethod(String methodName, String newQuery, boolean withConnection) {
        var argConnection = withConnection
            ? "connection, "
            : "";
        var typeName = type.getName();
        return
            "\tpublic static Optional<" + typeName + "> " + methodName + "(" + getParms(withConnection) + ") throws SQLException {\n" +
            "\t\treturn Optional.ofNullable((" + typeName + ")db." + methodName + "(" + argConnection + newQuery + ").get());\n" +
            "\t}\n";
    }

    private String getQueryMethods() {
        var methodName = hasArgs()
            ? "valueOf"
            : "valueOfAll";
        var args = hasArgs()
            ? ", " + getArgs()
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
