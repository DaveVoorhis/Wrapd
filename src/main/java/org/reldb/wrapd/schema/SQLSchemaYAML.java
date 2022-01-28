package org.reldb.wrapd.schema;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A SQLSchema with schema migrations defined in a YAML file.
 */
public class SQLSchemaYAML extends SQLSchema {
    private static final Msg MsgMissingMethodNameIn = new Msg("Missing method name in {0}.");
    private static final Msg MsgMethodMustHaveExpectedReturnType = new Msg("Method {0} must have return type Result.");
    private static final Msg MsgFoundMethodAndSettingItUpAsAnUpdate = new Msg("Found method: {0} and setting it up as an Update.");
    private static final Msg MsgUnrecognisedConstructFoundIn = new Msg("Unrecognised construct in {0}.");

    final private Update[] updates;

    /**
     <p>Define a schema creation/migration for a specified Database.</p>

     <p>The schema definitions and migrations are defined as a series of
     SQL queries and/or Java method invocations in YAML format. A simple
     example -- perhaps named <i>myschema.yaml</i> -- looks like this:</p>

     <pre>
     # Version 1
     - CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)

     # Version 2
     - CREATE TABLE $$tester02 (
         a INT NOT NULL PRIMARY KEY,
         b INT NOT NULL
       )
     </pre>

     <p>Note that the file defines an array of strings, where each string is a SQL query. Comments are
     purely informative and may be omitted.</p>

     <p><b>WARNING</b>: All new migrations <b>must</b> be added to the end or <b>schema migration will fail.</b></p>

     <p>It may be invoked using this snippet:</p>
     <pre>
     ...
     var testSchema = new SQLSchemaYAML(database, "myschema.yaml");
     var result = testSchema.setup(new ConsoleProgressIndicator());
     result.printIfError();
     ...
     </pre>

     <p>It will automatically create or migrate a schema referenced by <i>database</i>.</p>

     <p>More complex migrations may invoke Java methods in a SQLSchemaYAML-derived class. Define
     a class like the following:</p>
     <pre>
     	public static class TestSchema2 extends SQLSchemaYAML {
     		public TestSchema2(Database database, String yamlFileName) throws Throwable {
     			super(database, yamlFileName);
     		}
     		public Result doMyMethod(Integer x, String y) {
     			System.out.println(">>>> doMyMethod invoked with " + x + ", " + y);
     			return Result.OK;
     		}
     	}
     </pre>

     <p>Then a schema migration defined in <i>myschema2.yaml</i> can be invoked by the following:</p>
     <pre>
     ...
     var testSchema = new TestSchema2(database, "myschema2.yaml");
     var result = testSchema.setup(new ConsoleProgressIndicator());
     result.printIfError();
     ...
     </pre>

     <p>The <i>myschema2.yaml</i> file might be defined as follows:</p>
     <pre>
     # Version 1
     - CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)

     # Version 2 - invokes Java method in schema class
     - [doMyMethod, 2, "blah"]

     # Version 3
     - CREATE TABLE $$tester02 (
         a INT NOT NULL PRIMARY KEY,
         b INT NOT NULL
       )
     </pre>

     <p>Otherwise the same format as the first example, Java method invocations can be defined as
     arrays where the first array element is the method name and any subsequent elements are parameter
     arguments. Note that the Result-typed return value of the methods are ignored but must be provided.
     If you want a method to fail, throw an unchecked exception.</p>

     <p><b>WARNING</b>: All new migrations <b>must</b> be added to the end or <b>schema migration will fail.</b></p>

     @param yamlFileName YAML schema definition and migration file.
     @param database Database.
     @throws Throwable Error.
     */
    public SQLSchemaYAML(Database database, String yamlFileName) throws Throwable {
        super(database);
        var inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(yamlFileName);
        ArrayList<?> migrations = new Yaml().load(inputStream);
        var updateList = new ArrayList<Update>();
        for (var migration: migrations) {
            if (migration instanceof String) {
                var migrationQuery = (String)migration;
                updateList.add(schema -> {
                    database.updateAll(migrationQuery);
                    return Result.OK;
                });
            } else if (migration instanceof ArrayList) {
                var methodInvocationSpecification = (List<?>)migration;
                if (methodInvocationSpecification.size() < 1)
                    throw new FatalException(Str.ing(MsgMissingMethodNameIn, yamlFileName));
                String methodName = methodInvocationSpecification.get(0).toString();
                var arguments = new ArrayList<>();
                var argumentTypes = new ArrayList<Class<?>>();
                for (int index = 1; index < methodInvocationSpecification.size(); index++) {
                    var argument = methodInvocationSpecification.get(index);
                    arguments.add(argument);
                    argumentTypes.add(argument.getClass());
                }
                Method method;
                try {
                    method = getClass().getMethod(methodName, argumentTypes.toArray(new Class<?>[0]));
                } catch (NoSuchMethodException noSuchMethodException) {
                    argumentTypes.add(Object[].class);
                    arguments.add(new Object[0]);
                    method = getClass().getMethod(methodName, argumentTypes.toArray(new Class<?>[0]));
                }
                if (!method.getReturnType().isAssignableFrom(Result.class))
                    throw new FatalException(Str.ing(MsgMethodMustHaveExpectedReturnType, method));
                System.out.println(Str.ing(MsgFoundMethodAndSettingItUpAsAnUpdate, method));
                final Method invocationTarget = method;
                updateList.add(schema -> (Result)invocationTarget.invoke(this, arguments.toArray().clone()));
            } else
                throw new FatalException(Str.ing(MsgUnrecognisedConstructFoundIn, yamlFileName));
        }
        updates = updateList.toArray(new Update[0]);
    }

    @Override
    protected Update[] getUpdates() {
        return updates;
    }
}
