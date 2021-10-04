package org.reldb.wrapd.sqldb;

/**
 * Generates Java code to represent a query, which is a class that implements {@link Query}.
 */
public class QueryTypeGenerator extends SQLTypeGenerator {

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
        super(dir, packageSpec, queryName, sqlText, args);
    }

    /**
     * Set table name.
     *
     * @param tableName If not null, methods will be created to update/insert to this table.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private String getConstructor(String tupleTypeName) {
        return
            "\t@SuppressWarnings(\"unchecked\")\n" +
            "\tprotected " + getQueryName() + "(String queryText, Class<" + tupleTypeName + "> tupleClass, Object... arguments) {\n" +
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
        var methodName = hasArgs()
            ? ("query" + methodNameSuffix)
            : ("queryAll" + methodNameSuffix);
        var args = hasArgs()
            ? ", " + getArgs()
            : "";
        var newQuery = "new " + getQueryName() + "<>(sqlText, " + tupleTypeName + ".class" + args + ")";
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

    @Override
    protected String getDefinitionSourceCode() {
        var tupleTypeName = getQueryName() + "Tuple";
        return
            "package " + getPackageSpec() + ";\n\n" +
            "/* WARNING: Auto-generated code. DO NOT EDIT!!! */\n\n" +
            "import java.sql.SQLException;\n" +
            "import java.sql.Connection;\n" +
            "import java.util.stream.Stream;\n\n" +
            "import org.reldb.wrapd.sqldb.Tuple;\n" +
            "import org.reldb.wrapd.sqldb.Database;\n" +
            "import org.reldb.wrapd.sqldb.Query;\n\n" +
            "public class " + getQueryName() + "<T extends Tuple> extends Query<T> {\n" +
            "\tprivate final static String sqlText = \"" + getSQLText() + "\";\n\n" +
            getConstructor(tupleTypeName) +
            "\n" +
            getQueryMethods(tupleTypeName) +
            "}";
    }

}
