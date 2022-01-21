package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.InvalidValueException;
import org.reldb.wrapd.generator.JavaGenerator;

import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <p>Mechanism for defining Query, Update and valueOf classes.</p>
 *
 * <p>This is the main class you'll use to interact with Wrapd, at least to start with.</p>
 *
 * <p>This class turns SQL query definitions into Java methods, like having an assistant who
 * writes code for you.</p>
 *
 * <p>Use it like this:</p>
 *
 * <pre>
 * public class Definitions extends Definer {
 *
 *     public Definitions(Database database, String codeDirectory, String packageSpec) {
 *         super(database, codeDirectory, packageSpec);
 *     }
 *
 *     void generate() throws Throwable {
 *         // Delete everything in the target directory.
 *         purgeTarget();
 *
 *         // Generate definition for table ABC.
 *         defineTable("$$ABC");
 *
 *         // Generate definition for a query called JoinABCXYZWhere.
 *         defineQuery("JoinABCXYZWhere",
 *           "SELECT * FROM $$ABC, $$XYZ WHERE x = a AND x &gt; {lower} AND x &lt; {higher}", 2, 5);
 *
 *         // Generate a definition for a query called ValueOfXYZz that returns the value of the first
 *         // column of the first row of the result. (If there isn't one, the returned Option type is empty.)
 *         defineValueOf("ValueOfXYZz",
 *           "SELECT z FROM $$XYZ WHERE x = {xValue}", 33);
 *
 *         // Emit the above definitions in a single class called DatabaseAbstractionLayer, with
 *         // methods for each definition above.
 *         emitDatabaseAbstractionLayer("DatabaseAbstractionLayer");
 *     }
 *
 *     // Run the above queries; generate code to run them again via Java methods.
 *     public static void main(String[] args) throws Throwable {
 *         var database = new Database(...);
 *         var codeDirectory = "../path/to/code/in/project";
 *         var codePackage = "pkg.code.the";
 *         var sqlDefinitions = new Definitions(database, codeDirectory, codePackage);
 *         sqlDefinitions.generate();
 *         System.out.println("OK: Queries are ready.");
 *     }
 * }
 * </pre>
 *
 * Then take a look in the "../path/to/code/in/project" directory to see what it's generated. In particular,
 * in this example you'll want to look at DatabaseAbstractionLayer.java. You'll see that it contains method
 * definitions corresponding to the generated queries.
 */
public class Definer {
    private static final Msg MsgCodeDirectoryPurged = new Msg("Target {0} has been purged.");
    private static final Msg ErrCodeDirectoryPurgeFailed = new Msg("Unable to purge target {0}.");

    private final Database database;
    private final String codeDirectory;
    private final String packageSpec;
    private final Map<String, Collection<SQLTypeGenerator.Method>> methods = new HashMap<>();

    private void addMethods(String queryName, Collection<SQLTypeGenerator.Method> queryMethods) {
        methods.put(queryName, queryMethods);
    }

    /**
     * Create a Definer, given a Database, the directory where generated class definitions will be stored, and their package.
     *
     * @param database Database
     * @param codeDirectory Directory for generated class definitions.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     */
    public Definer(Database database, String codeDirectory, String packageSpec) {
        this.database = database;
        this.codeDirectory = codeDirectory;
        this.packageSpec = packageSpec;
    }

    /**
     * Get all the classes and their methods defined by using this Definer.
     *
     * @return Collection of method definitions for each defined class.
     */
    public Map<String, Collection<SQLTypeGenerator.Method>> getMethods() {
        return methods;
    }

    private static String lowerFirstCharacter(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    private static String upperFirstCharacter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Create a new class that provides to invoke all the methods defined using this Definer.
     *
     * Note that generated methods will always have (at least) a lower-case first character, in
     * keeping with Java convention. That means query names with an upper-case first character,
     * e.g., ClearABC, will emit a method called clearABC.
     *
     * @param newClassName Name of the generated database abstraction layer class definition.
     */
    public void emitDatabaseAbstractionLayer(String newClassName) {
        var source = new StringBuilder("package " + packageSpec + ";\n\n");
        source.append("import java.util.stream.*;\n");
        source.append("import java.sql.SQLException;\n");
        source.append("import java.util.Optional;\n");
        source.append("import org.reldb.wrapd.sqldb.Database;\n\n");
        source.append("public class ").append(newClassName).append(" {\n\n");
        source.append("\tprivate final Database database;\n\n");
        source.append("\tpublic ").append(newClassName).append("(Database database) {\n");
        source.append("\t\tthis.database = database;\n");
        source.append("\t}\n\n");
        source.append("\tpublic Database getDatabase() {\n");
        source.append("\t\treturn database;\n");
        source.append("\t}\n\n");
        getMethods().forEach((className, classMethods) -> classMethods.forEach(method -> {
            var returns = (method.returns == null) ? "void" : method.returns;
            var parmDefs = new StringBuilder();
            var parmNames = new StringBuilder("database");
            var parameters = method.parameters;
            for (int index = 1; index < parameters.size(); index++) {
                if (parmDefs.length() > 0)
                    parmDefs.append(", ");
                var parm = parameters.get(index);
                parmDefs.append(parm.type.getName()).append(" ").append(parm.name);
                parmNames.append(", ").append(parm.name);
            }
            var newMethodName = lowerFirstCharacter(className);
            source.append("\tpublic ").append(returns).append(" ").append(newMethodName).append(method.qualifier).append("(").append(parmDefs).append(") throws SQLException {\n");
            var returner = (method.returns == null) ? "" : "return ";
            source.append("\t\t").append(returner).append(className).append(".").append(method.name).append(method.qualifier).append("(").append(parmNames).append(");\n");
            source.append("\t}\n\n");
        }));
        source.append("}\n");
        var generator = new JavaGenerator(codeDirectory);
        generator.generateJavaCode(newClassName, packageSpec, source.toString());
    }

    /**
     * Prior to generating new code, purge <b>everything</b> in the package-specified
     * subdirectory of the code directory.
     *
     * This avoids possible build errors that may result from the build pipeline attempting
     * to compile outdated code. It's an optional operation in case the target
     * contains content that needs to be kept, though arguably it shouldn't be used that way.
     */
    public void purgeTarget() {
        var target = JavaGenerator.obtainDirectoryFromSourcePathAndPackage(codeDirectory, packageSpec);
        if (Directory.rmAll(target))
            System.out.println(Str.ing(MsgCodeDirectoryPurged, target));
        else
            System.err.println(Str.ing(ErrCodeDirectoryPurgeFailed, target));
    }

    /**
     * The result of Query generation.
     */
    public static class DefineQueryResult {
        /** Result of Tuple type generation. */
        public final TupleTypeGenerator.GenerateResult generateResult;

        /** Methods generated by this definition. */
        public final Collection<SQLTypeGenerator.Method> methods;

        /** Constructor.
         *
         * @param generateResult Result of Tuple type generation.
         * @param methods Methods generated by this definition.
         */
        public DefineQueryResult(TupleTypeGenerator.GenerateResult generateResult,
                                 Collection<SQLTypeGenerator.Method> methods) {
            this.generateResult = generateResult;
            this.methods = methods;
        }
    }

    /**
     * Define a Tuple type, and a Query class with query methods.
     *
     * @param queryName Name of query. Should be unique.
     * @param tableName Name of table the generated Tuple maps to. Null if not mapped to a table.
     *                  If not null, the Tuple type will have an insert(...) and an update(...)
     *                  for the specified table.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL query text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @return Result of generation.
     * @throws Throwable Error.
     */
    public DefineQueryResult defineQueryForTable(String queryName, String tableName, String sqlText, Object... args) throws Throwable {
        var tupleClassName = queryName + "Tuple";
        var queryGenerator = new QueryTypeGenerator(codeDirectory, packageSpec, tupleClassName, queryName, sqlText, args);
        queryGenerator.setTableName(tableName);
        queryGenerator.generate();
        var regeneratedSqlText = queryGenerator.getSQLText();
        var tupleClassCreated = (args == null || args.length == 0)
                ? database.createTupleFromQueryAllForUpdate(codeDirectory, packageSpec, tupleClassName, tableName, regeneratedSqlText)
                : database.createTupleFromQueryForUpdate(codeDirectory, packageSpec, tupleClassName, tableName, regeneratedSqlText, args);
        if (tupleClassCreated.isError())
            //noinspection ConstantConditions
            throw tupleClassCreated.error;
        addMethods(queryName, queryGenerator.getMethods());
        return new DefineQueryResult(tupleClassCreated.value, queryGenerator.getMethods());
    }

    /**
     * Define a Tuple type, and a Query class with query methods.
     *
     * @param queryName Name of query. Should be unique.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL query text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @return Result of generation.
     * @throws Throwable Error.
     */
    public DefineQueryResult defineQuery(String queryName, String sqlText, Object... args) throws Throwable {
        return defineQueryForTable(queryName, null, sqlText, args);
    }

    /**
     * Define an Update class with update(...) methods.
     *
     * WARNING: This will test the query by running it. Hope you're not doing this on a production database!
     *
     * @param queryName Name of update query. Should be unique.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL query text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow update query to succeed.
     * @return Result of generation.
     * @throws Throwable Error.
     */
    public Collection<SQLTypeGenerator.Method> defineUpdate(String queryName, String sqlText, Object... args) throws Throwable {
        var updateGenerator = new UpdateTypeGenerator(codeDirectory, packageSpec, queryName, sqlText, args);
        updateGenerator.generate();
        if (args == null || args.length == 0)
            database.updateAll(updateGenerator.getSQLText());
        else
            database.update(updateGenerator.getSQLText(), args);
        addMethods(queryName, updateGenerator.getMethods());
        return updateGenerator.getMethods();
    }

    /**
     * Define a Tuple type, and a Query class with insert(...) and update(...) methods
     * for the specified table. The Query is SELECT * FROM tableName WHERE whereClause.
     *
     * @param tableName Name of the table, optionally including $$. Note that the generated class
     *                  will have the same name as the table without $$ and with the first
     *                  character converted to upper case. E.g., a table name of $$blah will be
     *                  represented by a class named Blah.
     * @param whereClause The WHERE clause without the 'WHERE' keyword.
     *                    Parameters may be specified as ? or {name}. If {name} is used, it will
     *                    appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                    is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                    SQL query text with all {name} converted to ? for subsequent evaluation.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @return Result of generation.
     * @throws Throwable Error.
     */
    public DefineQueryResult defineTable(String tableName, String whereClause, Object... args) throws Throwable {
        var queryName = upperFirstCharacter(tableName.replaceAll("\\$\\$", ""));
        var realTableName = database.replaceTableNames(tableName);
        var query = "SELECT * FROM " + realTableName +
                (whereClause != null && !whereClause.isEmpty()
                        ? " WHERE " + whereClause
                        : "");
        return defineQueryForTable(queryName, realTableName, query, args);
    }

    /**
     * Define a Tuple type, and Query class that has an insert(...) and an update(...)
     * for the specified table. The Query is SELECT * FROM tableName.
     *
     * @param tableName Name of the table, optionally including $$.
     * @return Result of generation.
     * @throws Throwable Error.
     */
    public DefineQueryResult defineTable(String tableName) throws Throwable {
        return defineTable(tableName, null, (Object[])null);
    }

    /**
     * The result of ValueOf generation.
     */
    public static class DefineValueOfResult {
        /** Type of first column of result. */
        public final Class<?> type;

        /** Methods generated by this definition. */
        public final Collection<SQLTypeGenerator.Method> methods;

        /** Constructor.
         *
         * @param type Type of first column of result.
         * @param methods Methods generated by this definition.
         */
        public DefineValueOfResult(Class<?> type, Collection<SQLTypeGenerator.Method> methods) {
            this.type = type;
            this.methods = methods;
        }
    }

    /**
     * Define a ValueOf class with methods that return the first column of the first row.
     *
     * @param queryName Name of valueOf query. Should be unique.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL query text with all {name} converted to ? for subsequent evaluation.
     *                For optimum performance, should have one and only one column and return one row.
     * @param args Arguments that specify parameter type(s) and allow query to succeed.
     * @return Result of generation.
     * @throws Throwable Error.
     */
    public DefineValueOfResult defineValueOf(String queryName, String sqlText, Object... args) throws Throwable {
        var parameterConverter = new SQLParameterConverter(sqlText);
        parameterConverter.process();
        var type = database.getTypeOfFirstColumn(parameterConverter.getSQLText(), args);
        var valueOfGenerator = new ValueOfTypeGenerator(codeDirectory, packageSpec, queryName, type, sqlText, args);
        valueOfGenerator.generate();
        addMethods(queryName, valueOfGenerator.getMethods());
        return new DefineValueOfResult(type, valueOfGenerator.getMethods());
    }

    /**
     * Given the filespec of a YAML query definition file -- which actually specifies invocations of any
     * Definer methods (including define) -- run it to define queries.
     *
     * @param yamlFileName YAML query definition file.
     * @throws Throwable Error.
     */
    public void define(String yamlFileName) throws Throwable {
        var inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(yamlFileName);
        Map<String, Map<String, List<?>>> queryDefs = new Yaml().load(inputStream);
        for (var queryDefinitionGroup: queryDefs.entrySet()) {
            String methodName = queryDefinitionGroup.getKey();
            System.out.println("Query definition method name = " + methodName);
            var queryDefinitions = queryDefinitionGroup.getValue();
            for (var queryDefinition: queryDefinitions.entrySet()) {
                var queryName = queryDefinition.getKey();
                var definitionArguments = queryDefinition.getValue();
                System.out.println("\t" + queryName + ": " + definitionArguments);
                var argTypes = new ArrayList<Class<?>>();
                var args = new ArrayList<>();
                argTypes.add(queryName.getClass());
                args.add(queryName);
                boolean varargsFound = false;
                for (var argument: definitionArguments) {
                    if (argument instanceof List && args.size() == definitionArguments.size()) {
                        argTypes.add(Object[].class);
                        args.add(((List<?>)argument).toArray());
                        varargsFound = true;
                    } else {
                        argTypes.add(argument.getClass());
                        args.add(argument);
                    }
                }
                Method method = null;
                try {
                    method = Definer.class.getMethod(methodName, argTypes.toArray(new Class<?>[0]));
                } catch (NoSuchMethodException noSuchMethodException) {
                    if (varargsFound)
                        throw noSuchMethodException;
                    argTypes.add(Object[].class);
                    args.add(new Object[0]);
                    method = Definer.class.getMethod(methodName, argTypes.toArray(new Class<?>[0]));
                }
                System.out.println("\tFound method: " + method + " and invoking it.");
                method.invoke(this, args.toArray());
            }
        }
    }
}
