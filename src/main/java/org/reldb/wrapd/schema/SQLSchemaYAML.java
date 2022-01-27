package org.reldb.wrapd.schema;

import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Definer;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A SQLSchema with schema migrations defined in a YAML file.
 */
public class SQLSchemaYAML extends SQLSchema {
    final private Update[] updates;

    /**
     * Create an instance of a schema for a specified Database.
     *
     * @param yamlFileName YAML schema definition and migration file.
     * @param database Database.
     * @throws Throwable
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
                    throw new FatalException("Missing method name in " + yamlFileName);
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
                    throw new FatalException("Method " + method + " must have return type Result.");
                System.out.println("Found method: " + method + " and setting it up as an Update.");
                final Method invocationTarget = method;
                updateList.add(schema -> (Result)invocationTarget.invoke(this, arguments.toArray().clone()));
            } else
                throw new FatalException("Unrecognised construct in " + yamlFileName);
        }
        updates = updateList.toArray(new Update[0]);
    }

    @Override
    protected Update[] getUpdates() {
        return updates;
    }
}
