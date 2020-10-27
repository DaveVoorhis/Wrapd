package org.reldb.wrapd.sqldb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;
import org.reldb.wrapd.tuples.Tuple;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tools for creating Tuple-derived classes from ResultSetS and for turning ResultSetS into Tuple-derived instances for processing directly or as a List or Stream.
 *
 * @author dave
 */
public class ResultSetToTuple {

    private static Logger log = LogManager.getLogger(ResultSetToTuple.class.toString());

    /**
     * Given a target code directory and a desired Tuple class name, and a ResultSet, generate a Tuple class
     * to host the ResultSet. This will normally be invoked in a setup/build phase run
     *
     * @param codeDir   - Directory where source code will be stored.
     * @param tupleName - Name of new Tuple class.
     * @param results   - ResultSet to be used to create the new Tuple class.
     * @return - CompilationResults.
     * @throws SQLException             - thrown if there is a problem retrieving ResultSet metadata.
     * @throws ClassNotFoundException   - thrown if a column class specified in the ResultSet metadata can't be loaded.
     * @throws IllegalArgumentException - thrown if an argument is null
     */
    public static CompilationResults createTuple(String codeDir, String tupleName, ResultSet results) throws SQLException, ClassNotFoundException {
        if (codeDir == null)
            throw new IllegalArgumentException("codeDir may not be null");
        if (tupleName == null)
            throw new IllegalArgumentException("tupleName may not be null");
        if (results == null)
            throw new IllegalArgumentException("results may not be null");
        var generator = new TupleTypeGenerator(codeDir, tupleName);
        var metadata = results.getMetaData();
        for (int column = 1; column <= metadata.getColumnCount(); column++) {
            var name = metadata.getColumnName(column);
            var columnClassName = metadata.getColumnClassName(column);
            var type = Class.forName(columnClassName);
            generator.addAttribute(name, type);
        }
        return generator.compile();
    }

    /**
     * FunctionalInterface to define lambdas for processing each Tuple in a ResultSet.
     */
    @FunctionalInterface
    public interface TupleProcessor<T extends Tuple> {
        public void process(T tupleType);
    }

    /**
     * Iterate a ResultSet, unmarshall each row into a Tuple, and pass it to a TupleProcessor for processing.
     *
     * @param resultSet
     * @param tupleType
     * @param tupleProcessor
     * @throws SecurityException         - thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException     - thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException - thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException  - thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or a null argument
     * @throws IllegalAccessException    - thrown if unable to instantiate tuple class
     * @throws InstantiationException    - thrown if unable to instantiate tuple class
     * @throws SQLException              - thrown if accessing ResultSet fails
     * @throws NoSuchFieldException      - thrown if a given ResultSet field name cannot be found in the Tuple
     */
    public static <T extends Tuple> void process(ResultSet resultSet, Class<T> tupleType, TupleProcessor<T> tupleProcessor) throws NoSuchMethodException, SecurityException, SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        if (resultSet == null)
            throw new IllegalArgumentException("resultSet may not be null");
        if (tupleType == null)
            throw new IllegalArgumentException("tupleType may not be null");
        if (tupleProcessor == null)
            throw new IllegalArgumentException("tupleProcessor may not be null");
        var tupleConstructor = tupleType.getConstructor((Class<?>[]) null);
        var metadata = resultSet.getMetaData();
        boolean optimised = false;
        Field[] fields = null;
        while (resultSet.next()) {
            var tuple = tupleConstructor.newInstance((Object[]) null);
            if (optimised) {
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    var value = resultSet.getObject(column);
                    fields[column].set(tuple, value);
                }
            } else {
                int columnCount = metadata.getColumnCount();
                fields = new Field[columnCount + 1];
                for (int column = 1; column <= columnCount; column++) {
                    var name = metadata.getColumnName(column);
                    var value = resultSet.getObject(column);
                    var field = tuple.getClass().getField(name);
                    field.set(tuple, value);
                    fields[column] = field;
                }
                optimised = true;
            }
            tupleProcessor.process(tuple);
        }
    }

    /**
     * Convert a ResultSet to a List of TupleS.
     *
     * @param resultSet
     * @param tupleType
     * @return List<? extends Tuple>
     * @throws SecurityException         - thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException     - thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException - thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException  - thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException    - thrown if unable to instantiate tuple class
     * @throws InstantiationException    - thrown if unable to instantiate tuple class
     * @throws SQLException              - thrown if accessing ResultSet fails
     * @throws NoSuchFieldException      - thrown if a given ResultSet field name cannot be found in the Tuple
     */
    public static <T extends Tuple> List<T> toList(ResultSet resultSet, Class<T> tupleType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchFieldException {
        var rows = new LinkedList<T>();
        process(resultSet, tupleType, tuple -> rows.add(tuple));
        return rows;
    }

    /**
     * Convert a ResultSet to a List of TupleS, each configured for a possible future update.
     *
     * @param resultSet
     * @param tupleType
     * @return List<? extends Tuple>
     * @throws SecurityException         - thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException     - thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException - thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException  - thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException    - thrown if unable to instantiate tuple class
     * @throws InstantiationException    - thrown if unable to instantiate tuple class
     * @throws SQLException              - thrown if accessing ResultSet fails
     * @throws NoSuchFieldException      - thrown if a given ResultSet field name cannot be found in the Tuple
     */
    public static <T extends Tuple> List<T> toListForUpdate(ResultSet resultSet, Class<T> tupleType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchFieldException {
        var rows = new LinkedList<T>();
        process(resultSet, tupleType, tuple -> {
            try {
                tuple.backup();
            } catch (CloneNotSupportedException e) {
                log.error("ERROR: toListForUpdate: Unable to clone tuple of type " + tupleType.getName() + ": " + e);
            }
            rows.add(tuple);
        });
        return rows;
    }

    /**
     * Convert a ResultSet to a Stream of TupleS.
     *
     * @param resultSet - source ResultSet
     * @param tupleType - subclass of Tuple. Each row will be converted to a new instance of this class.
     * @return Stream<? extends Tuple>.
     * @throws SecurityException         - thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException     - thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException - thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException  - thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException    - thrown if unable to instantiate tuple class
     * @throws InstantiationException    - thrown if unable to instantiate tuple class
     * @throws SQLException              - thrown if accessing ResultSet fails
     * @throws NoSuchFieldException      - thrown if a given ResultSet field name cannot be found in the Tuple
     */
    public static <T extends Tuple> Stream<T> toStream(ResultSet resultSet, Class<T> tupleType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchFieldException {
        return toList(resultSet, tupleType).stream();
    }

    /**
     * Convert a ResultSet to a Stream of TupleS, each configured for a possible future update.
     *
     * @param resultSet - source ResultSet
     * @param tupleType - subclass of Tuple. Each row will be converted to a new instance of this class.
     * @return Stream<? extends Tuple>.
     * @throws SecurityException         - thrown if tuple constructor is not accessible
     * @throws NoSuchMethodException     - thrown if tuple constructor doesn't exist
     * @throws InvocationTargetException - thrown if unable to instantiate tuple class
     * @throws IllegalArgumentException  - thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values, or null arguments
     * @throws IllegalAccessException    - thrown if unable to instantiate tuple class
     * @throws InstantiationException    - thrown if unable to instantiate tuple class
     * @throws SQLException              - thrown if accessing ResultSet fails
     * @throws NoSuchFieldException      - thrown if a given ResultSet field name cannot be found in the Tuple
     */
    public static <T extends Tuple> Stream<T> toStreamForUpdate(ResultSet resultSet, Class<T> tupleType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchFieldException {
        return toListForUpdate(resultSet, tupleType).stream();
    }

    /**
     * Eliminate the tuple with a given name.
     *
     * @param codeDir   - Directory where source code will be stored.
     * @param tupleName - Name of tuple class.
     */
    public static void destroyTuple(String codeDir, String tupleName) {
        if (codeDir == null)
            throw new IllegalArgumentException("codeDir may not be null");
        if (tupleName == null)
            throw new IllegalArgumentException("tupleName may not be null");
        TupleTypeGenerator.destroy(codeDir, tupleName);
    }

}
