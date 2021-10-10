package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.response.Response;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tools for creating Tuple-derived classes from ResultSetS and for turning ResultSetS into Tuple-derived instances for processing directly or as a List or Stream.
 */
public class ResultSetToTuple {
    private final static Msg ErrNullCodeDir = new Msg("codeDir may not be null.", ResultSetToTuple.class);
    private final static Msg ErrNullPackageSpec = new Msg("packageSpec may not be null.", ResultSetToTuple.class);
    private final static Msg ErrNullTupleName = new Msg("tupleName may not be null.", ResultSetToTuple.class);
    private final static Msg ErrNullResults = new Msg("results may not be null.", ResultSetToTuple.class);
    private final static Msg ErrNullResultSet = new Msg("resultSet may not be null", ResultSetToTuple.class);
    private final static Msg ErrNullTupleType = new Msg("tupleType may not be null", ResultSetToTuple.class);
    private final static Msg ErrNullTupleProcessor = new Msg("tupleProcessor may not be null", ResultSetToTuple.class);
    private final static Msg ErrNullDatabase = new Msg("database may not be null", ResultSetToTuple.class);
    private final static Msg ErrFailedToLoadClass = new Msg("Unable to load column class", ResultSetToTuple.class);
    private final static Msg ErrNoColumnsInResultSet = new Msg("ResultSet contains no columns.", ResultSetToTuple.class);

    /**
     * A functional interface for defining lambda expressions that do something with an
     * attribute name and an attribute type.
     */
    @FunctionalInterface
    public interface AttributeReceiver {
        /**
         * Process an attribute.
         *
         * @param name Name of attribute.
         * @param type Type of attribute.
         */
        void process(String name, Class<?> type);
    }

    /**
     * Do something with each attribute (i.e., name/class pair) of a ResultSet.
     *
     * @param results A ResultSet.
     * @param customisations Customisations for specific DBMS types.
     * @param receiver The lambda that will receive each attribute.
     * @throws SQLException thrown if there is a problem retrieving ResultSet metadata.
     * @throws ClassNotFoundException thrown if a column class specified in the ResultSet metadata can't be loaded.
     */
    public static void processResultSetAttributes(ResultSet results, Customisations customisations, AttributeReceiver receiver) throws SQLException, ClassNotFoundException {
        var metadata = results.getMetaData();
        for (var column = 1; column <= metadata.getColumnCount(); column++) {
            var name = metadata.getColumnName(column);
            var sqlTypeName = metadata.getColumnTypeName(column);
            var columnClassName = metadata.getColumnClassName(column);
            if (customisations != null)
                columnClassName = customisations.getSpecificColumnClass(sqlTypeName);
            var type = Class.forName(columnClassName);
            receiver.process(name, type);
        }
    }

    /**
     * Obtain the type of the first column of a ResultSet. Used in ValueOf.
     *
     * @param results A ResultSet.
     * @param customisations Customisations for specific DBMS types.
     * @return The type of the first column of the ResultSet, or an error, wrapped in a Response.
     */
    public static Response<Class<?>> obtainTypeOfFirstColumnOfResultSet(ResultSet results, Customisations customisations) {
        var types = new LinkedList<Class<?>>();
        try {
            processResultSetAttributes(results, customisations, (name, type) -> types.add(type));
        } catch (Throwable e) {
            return Response.set(new SQLException(Str.ing(ErrFailedToLoadClass), e));
        }
        if (types.size() > 0)
            return Response.set(types.get(0));
        return Response.set(new SQLException(Str.ing(ErrNoColumnsInResultSet)));
    }

    /**
     * Given a target code directory and a desired UpdatableTuple class name, and a ResultSet,
     * generate an UpdatableTuple class to host the ResultSet. This will normally be invoked in a setup/build phase run.
     *
     * @param codeDir Directory where source code will be stored.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleName Name of new UpdatableTuple class.
     * @param results ResultSet to be used to create the new UpdatableTuple class.
     * @param customisations Customisations for specific DBMS types.
     * @param tableName Name of table this Tuple maps to. Null if not mapped to a table.
     * @return Result of Tuple generation.
     * @throws SQLException thrown if there is a problem retrieving ResultSet metadata.
     * @throws ClassNotFoundException thrown if a column class specified in the ResultSet metadata can't be loaded.
     * @throws IllegalArgumentException thrown if an argument is null
     */
    public static TupleTypeGenerator.GenerateResult createTupleForUpdate(String codeDir, String packageSpec, String tupleName, ResultSet results, Customisations customisations, String tableName) throws SQLException, ClassNotFoundException {
        if (codeDir == null)
            throw new IllegalArgumentException(Str.ing(ErrNullCodeDir));
        if (packageSpec == null)
            throw new IllegalArgumentException(Str.ing(ErrNullPackageSpec));
        if (tupleName == null)
            throw new IllegalArgumentException(Str.ing(ErrNullTupleName));
        if (results == null)
            throw new IllegalArgumentException(Str.ing(ErrNullResults));
        var generator = new TupleTypeGenerator(codeDir, packageSpec, tupleName);
        generator.setTableName(tableName);
        processResultSetAttributes(results, customisations, generator::addAttribute);
        return generator.generate();
    }

    /**
     * Given a target code directory and a desired Tuple class name, and a ResultSet, generate a Tuple class
     * to host the ResultSet. This will normally be invoked in a setup/build phase run.
     *
     * @param codeDir Directory where source code will be stored.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleName Name of new Tuple class.
     * @param results ResultSet to be used to create the new Tuple class.
     * @param customisations Customisations for specific DBMS types.
     * @return Result of Tuple generation.
     * @throws SQLException thrown if there is a problem retrieving ResultSet metadata.
     * @throws ClassNotFoundException thrown if a column class specified in the ResultSet metadata can't be loaded.
     * @throws IllegalArgumentException thrown if an argument is null
     */
    public static TupleTypeGenerator.GenerateResult createTuple(String codeDir, String packageSpec, String tupleName, ResultSet results, Customisations customisations) throws SQLException, ClassNotFoundException {
        return createTupleForUpdate(codeDir, packageSpec, tupleName, results, customisations, null);
    }

    /**
     * FunctionalInterface to define lambdas for processing each Tuple in a ResultSet.
     */
    @FunctionalInterface
    public interface TupleProcessor<T extends Tuple> {
        /**
         * Process a tuple.
         *
         * @param tupleType A tuple of type T (which extends Tuple.)
         * @throws Throwable Any error generated by processing.
         */
        void process(T tupleType) throws Throwable;
    }

    private static <T extends Tuple> Field[] populateTuple(ResultSetMetaData metadata, ResultSet resultSet, Field[] fields, T tuple) throws SQLException, NoSuchFieldException, IllegalAccessException {
        if (fields != null) {
            for (var column = 1; column <= metadata.getColumnCount(); column++) {
                var value = resultSet.getObject(column);
                fields[column].set(tuple, value);
            }
        } else {
            var columnCount = metadata.getColumnCount();
            fields = new Field[columnCount + 1];
            for (var column = 1; column <= columnCount; column++) {
                var name = metadata.getColumnName(column);
                var value = resultSet.getObject(column);
                var field = tuple.getClass().getField(name);
                field.set(tuple, value);
                fields[column] = field;
            }
        }
        return fields;
    }

    /**
     * Iterate a ResultSet, unmarshall each row into a Tuple, and pass it to a TupleProcessor for processing.
     *
     * @param <T> Tuple type.
     * @param resultSet ResultSet to iterate
     * @param tupleType tuple type
     * @param tupleProcessor tuple processor
     * @throws SecurityException thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or a null argument
     * @throws IllegalAccessException thrown if unable to instantiate tuple class
     * @throws InstantiationException thrown if unable to instantiate tuple class
     * @throws SQLException thrown if accessing ResultSet fails
     * @throws NoSuchFieldException thrown if a given ResultSet field name cannot be found in the Tuple
     * @throws CloneNotSupportedException thrown af a Tuple cannot be cloned to create a backup
     */
    public static <T extends Tuple> void process(ResultSet resultSet, Class<T> tupleType, TupleProcessor<T> tupleProcessor) throws Throwable {
        if (resultSet == null)
            throw new IllegalArgumentException(Str.ing(ErrNullResultSet));
        if (tupleType == null)
            throw new IllegalArgumentException(Str.ing(ErrNullTupleType));
        if (tupleProcessor == null)
            throw new IllegalArgumentException(Str.ing(ErrNullTupleProcessor));
        var tupleConstructor = tupleType.getConstructor();
        var metadata = resultSet.getMetaData();
        Field[] fields = null;
        while (resultSet.next()) {
            var tuple = tupleConstructor.newInstance();
            fields = populateTuple(metadata, resultSet, fields, tuple);
            tupleProcessor.process(tuple);
        }
    }

    /**
     * Iterate a ResultSet, unmarshall each row into a Tuple, and pass it to a TupleProcessor for processing.
     *
     * @param <T> UpdatableTuple type.
     * @param database The Database that issued the query that produced the ResultSet.
     * @param resultSet ResultSet to iterate
     * @param tupleType tuple type
     * @param tupleProcessor tuple processor
     * @throws SecurityException thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or a null argument
     * @throws IllegalAccessException thrown if unable to instantiate tuple class
     * @throws InstantiationException thrown if unable to instantiate tuple class
     * @throws SQLException thrown if accessing ResultSet fails
     * @throws NoSuchFieldException thrown if a given ResultSet field name cannot be found in the Tuple
     * @throws CloneNotSupportedException thrown af a Tuple cannot be cloned to create a backup
     */
    public static <T extends UpdatableTuple> void processForUpdate(Database database, ResultSet resultSet, Class<T> tupleType, TupleProcessor<T> tupleProcessor) throws Throwable {
        if (database == null)
            throw new IllegalArgumentException(Str.ing(ErrNullDatabase));
        if (resultSet == null)
            throw new IllegalArgumentException(Str.ing(ErrNullDatabase));
        if (tupleType == null)
            throw new IllegalArgumentException(Str.ing(ErrNullTupleType));
        if (tupleProcessor == null)
            throw new IllegalArgumentException(Str.ing(ErrNullTupleProcessor));
        var tupleConstructor = tupleType.getConstructor(Database.class);
        var metadata = resultSet.getMetaData();
        Field[] fields = null;
        while (resultSet.next()) {
            var tuple = tupleConstructor.newInstance(database);
            fields = populateTuple(metadata, resultSet, fields, tuple);
            tuple.backup();
            tupleProcessor.process(tuple);
        }
    }

    /**
     * Convert a ResultSet to a List of TupleS.
     *
     * @param <T> Tuple type.
     * @param resultSet ResultSet to iterate
     * @param tupleType tuple type
     * @return List&lt;? extends Tuple&gt; List of tuples returned
     * @throws SecurityException thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException thrown if unable to instantiate tuple class
     * @throws InstantiationException thrown if unable to instantiate tuple class
     * @throws SQLException thrown if accessing ResultSet fails
     * @throws NoSuchFieldException thrown if a given ResultSet field name cannot be found in the Tuple
     * @throws CloneNotSupportedException thrown af a Tuple cannot be cloned to create a backup
     */
    public static <T extends Tuple> List<T> toList(ResultSet resultSet, Class<T> tupleType) throws Throwable {
        var rows = new LinkedList<T>();
        process(resultSet, tupleType, rows::add);
        return rows;
    }

    /**
     * Convert a ResultSet to a List of UpdatableTupleS, each configured for a possible future update.
     *
     * @param <T> UpdatableTuple type.
     * @param database The Database that issued the query that produced the ResultSet.
     * @param resultSet ResultSet to iterate
     * @param tupleType tuple type
     * @return List&lt;? extends UpdatableTuple&gt; List of tuples returned
     * @throws SecurityException thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException thrown if unable to instantiate tuple class
     * @throws InstantiationException thrown if unable to instantiate tuple class
     * @throws SQLException thrown if accessing ResultSet fails
     * @throws NoSuchFieldException thrown if a given ResultSet field name cannot be found in the Tuple
     * @throws CloneNotSupportedException thrown af a Tuple cannot be cloned to create a backup
     */
    public static <T extends UpdatableTuple> List<T> toListForUpdate(Database database, ResultSet resultSet, Class<T> tupleType) throws Throwable {
        var rows = new LinkedList<T>();
        processForUpdate(database, resultSet, tupleType, rows::add);
        return rows;
    }

    /**
     * Convert a ResultSet to a Stream of TupleS.
     *
     * @param <T> Tuple type.
     * @param resultSet source ResultSet
     * @param tupleType subclass of Tuple. Each row will be converted to a new instance of this class.
     * @return Stream&lt;? extends Tuple&gt;.
     * @throws SecurityException thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException thrown if unable to instantiate tuple class
     * @throws InstantiationException thrown if unable to instantiate tuple class
     * @throws SQLException thrown if accessing ResultSet fails
     * @throws NoSuchFieldException thrown if a given ResultSet field name cannot be found in the Tuple
     * @throws CloneNotSupportedException thrown af a Tuple cannot be cloned to create a backup
     */
    public static <T extends Tuple> Stream<T> toStream(ResultSet resultSet, Class<T> tupleType) throws Throwable {
        return toList(resultSet, tupleType).stream();
    }

    /**
     * Convert a ResultSet to a Stream of UpdatableTupleS, each configured for a possible future update.
     *
     * @param <T> UpdatableTuple type.
     * @param database The Database that issued the query that produced the ResultSet.
     * @param resultSet source ResultSet
     * @param tupleType subclass of UpdatableTuple. Each row will be converted to a new instance of this class.
     * @return Stream&lt;? extends UpdatableTuple&gt;.
     * @throws SecurityException thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException thrown if unable to instantiate tuple class
     * @throws InstantiationException thrown if unable to instantiate tuple class
     * @throws SQLException thrown if accessing ResultSet fails
     * @throws NoSuchFieldException thrown if a given ResultSet field name cannot be found in the Tuple
     * @throws CloneNotSupportedException thrown af a Tuple cannot be cloned to create a backup
     */
    public static <T extends UpdatableTuple> Stream<T> toStreamForUpdate(Database database, ResultSet resultSet, Class<T> tupleType) throws Throwable {
        return toListForUpdate(database, resultSet, tupleType).stream();
    }

    /**
     * Eliminate the tuple with a given name.
     *
     * @param codeDir Directory where source code will be stored.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleName Name of tuple class.
     * @return True if source code and generated class have been deleted.
     */
    public static boolean destroyTuple(String codeDir, String packageSpec, String tupleName) {
        if (codeDir == null)
            throw new IllegalArgumentException(Str.ing(ErrNullCodeDir));
        if (packageSpec == null)
            throw new IllegalArgumentException(Str.ing(ErrNullPackageSpec));
        if (tupleName == null)
            throw new IllegalArgumentException(Str.ing(ErrNullTupleName));
        return new TupleTypeGenerator(codeDir, packageSpec, tupleName).destroy();
    }

}
