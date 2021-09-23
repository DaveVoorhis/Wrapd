package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.events.EventHandler;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.response.Response;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.tuples.Tuple;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Database access layer.
 */
public class Database {

    private final DataSource dataSource;

    private final String dbTablenamePrefix;
    private final Customisations customisations;

    /**
     * An instance of an SQL query, for monitoring queries processed by a Database.
     */
    public static class SQLEvent {
        /** The SQL text of the query. */
        public final String sqlText;

        /** Where the query was issued. */
        public final String location;

        /**
         * Constructor.
         *
         * @param location Where the query was issued.
         * @param sqlText The SQL text of the query.
         */
        public SQLEvent(String location, String sqlText) {
            this.location = location;
            this.sqlText = sqlText;
        }
    }

    /** Subscribe to monitor queries processed by a Database. */
    public final EventHandler<SQLEvent> sqlEvents = new EventHandler<>();

    /**
     * Distribute a SQLEvent to interested listeners.
     *
     * @param location Where the query was generated or processed.
     * @param query The SQL text of the query.
     */
    protected void distributeSQLEvent(String location, String query) {
        sqlEvents.distribute(new SQLEvent(location, query));
    }

    /**
     * Construct a Database.
     *
     * @param dataSource Data source
     * @param dbTablenamePrefix Table name prefix
     * @param customisations DBMS-specific customisations
     */
    public Database(DataSource dataSource, String dbTablenamePrefix, Customisations customisations) {
        this.dataSource = dataSource;
        this.dbTablenamePrefix = nullToEmptyString(dbTablenamePrefix);
        this.customisations = customisations;
    }

    public String toString() {
        return "Database: " + dataSource.toString();
    }

    /**
     * Wherever $$ appears in the argument, replace it with dbTableNamePrefix.
     *
     * @param query The source text.
     * @return The source text with every $$ replaced with the contents of dbTablenamePrefix.
     */
    public String replaceTableNames(String query) {
        return query.replaceAll("\\$\\$", dbTablenamePrefix);
    }

    /**
     * Used to define lambda expressions that make use of a Connection and return a value of type T.
     *
     * @param <T> Return type of go(...)
     */
    @FunctionalInterface
    public interface ConnectionUser<T> {
        /**
         * Use a connection.
         *
         * @param c Connection.
         * @return Specified return type.
         * @throws SQLException thrown if operation fails.
         */
        T go(Connection c) throws SQLException;
    }

    /**
     * Use a connection.
     *
     * @param <T> Type of return value from use of connection.
     * @param connectionUser Instance of ConnectionUser, usually as a lambda expression.
     * @return A Response&lt;T&gt; containing either a T (indicating success) or a SQLException.
     * @throws SQLException Error obtaining connection.
     */
    public <T> Response<T> processConnection(ConnectionUser<T> connectionUser) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try {
                return new Response<>(connectionUser.go(conn));
            } catch (SQLException t) {
                return new Response<>(t);
            }
        }
    }

    /**
     * Use a connection.
     *
     * @param <T> Type of return value from user of connection.
     * @param connectionUser Instance of ConnectionUser, usually as a lambda expression.
     * @return A value of type T as a result of using a Connection.
     * @throws SQLException Error.
     */
    public <T> T useConnection(ConnectionUser<T> connectionUser) throws SQLException {
        var result = processConnection(connectionUser);
        if (result.error != null)
            throw (SQLException) result.error;
        return result.value;
    }

    /**
     * Used to define lambda expressions that receive a ResultSet for processing. T specifies the type of the return value from processing the ResultSet.
     *
     * @param <T> Return type of go(...)
     */
    @FunctionalInterface
    public interface ResultSetReceiver<T> {
        /**
         * Process a ResultSet.
         *
         * @param r ResultSet to process.
         * @return Specified return type, wrapped in a Response.
         */
        Response<T> go(ResultSet r);
    }

    /**
     * Issue an update query.
     *
     * @param connection Database connection.
     * @param sqlStatement SQL query.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean updateAll(Connection connection, String sqlStatement) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            var sqlized = replaceTableNames(sqlStatement);
            distributeSQLEvent("updateAll: ", sqlized);
            return statement.execute(sqlized);
        }
    }

    /**
     * Issue an update query.
     *
     * @param sqlStatement SQL update query.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean updateAll(String sqlStatement) throws SQLException {
        return useConnection(conn -> updateAll(conn, sqlStatement));
    }

    /**
     * Issue a SELECT query and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param connection Database connection.
     * @param query SELECT query.
     * @param columnName Column name from which to retrieve first row's value.
     * @return Value of first row in columnName.
     * @throws SQLException Error.
     */
    public Optional<?> valueOfAll(Connection connection, String query, String columnName) throws SQLException {
        return queryAll(connection, query, rs -> {
            try {
                if (rs.next())
                    return new Response<>(Optional.ofNullable(rs.getObject(columnName)));
                return new Response<>(Optional.empty());
            } catch (SQLException sqe) {
                return new Response<>(sqe);
            }
        });
    }

    /**
     * Issue a SELECT query and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param query SELECT query.
     * @param columnName Column name from which to retrieve first row's value.
     * @return Value of first row in columnName.
     * @throws SQLException Error.
     */
    public Optional<?> valueOfAll(String query, String columnName) throws SQLException {
        return useConnection(conn -> valueOfAll(conn, query, columnName));
    }

    /**
     * Represents an SQL NULL on behalf of a specified SQL type from the {@link java.sql.Types} enum.
     */
    private static class Null {
        /** The {@link java.sql.Types} enum value for this Null. */
        final int type;

        /**
         * Create a Null for a given type.
         *
         * @param type Type value from the {@link java.sql.Types} enum.
         */
        Null(int type) {
            this.type = type;
        }
    }

    // Canonical setup of prepared statement parameters from Java types.
    private static void setupParms(PreparedStatement statement, Object... parms) throws SQLException {
        int parmNumber = 1;
        for (Object parm : parms) {
            if (parm == null)
                statement.setNull(parmNumber, Types.VARCHAR);
            else if (parm instanceof Null)
                statement.setNull(parmNumber, ((Null) parm).type);
            else if (parm instanceof Integer)
                statement.setInt(parmNumber, (Integer) parm);
            else if (parm instanceof Double)
                statement.setDouble(parmNumber, (Double) parm);
            else if (parm instanceof Float)
                statement.setFloat(parmNumber, (Float) parm);
            else if (parm instanceof Date)
                statement.setDate(parmNumber, (Date) parm);
            else if (parm instanceof Long)
                statement.setLong(parmNumber, (Long) parm);
            else if (parm instanceof Blob)
                statement.setBlob(parmNumber, (Blob) parm);
            else if (parm instanceof Boolean)
                statement.setBoolean(parmNumber, (Boolean) parm);
            else
                statement.setString(parmNumber, parm.toString());
            parmNumber++;
        }
    }

    /**
     * Used to define lambda expressions that make use of a PreparedStatement and return a value of type T.
     *
     * @param <T> Return type of go(...)
     */
    @FunctionalInterface
    public interface PreparedStatementUser<T> {
        /**
         * Execute a prepared statement.
         *
         * @param ps The PreparedStatement.
         * @return Specified result.
         * @throws SQLException thrown if operation fails.
         */
        T go(PreparedStatement ps) throws SQLException;
    }

    /**
     * Use a prepared statement.
     *
     * @param <T> Type of return value from user of connection.
     * @param preparedStatementUser Instance of PreparedStatementUser, usually as a lambda expression.
     * @param connection Database connection.
     * @param query SQL query, with optional parametric indicators (usually ?)
     * @param parms Object[] of parameter arguments.
     * @return A value of type T as a result of using a PreparedStatement.
     * @throws SQLException Error.
     */
    public <T> Response<T> processPreparedStatement(PreparedStatementUser<T> preparedStatementUser, Connection connection, String query, Object... parms) throws SQLException {
        var sqlized = replaceTableNames(query);
        distributeSQLEvent("processPreparedStatement: ", sqlized);
        int argCount = parms.length;
        int parmCount = (int) sqlized.chars().filter(ch -> ch == '?').count();
        if (argCount != parmCount)
            throw new IllegalArgumentException("ERROR: processPreparedStatement: Number of parameters (" + parmCount + ") doesn't match number of arguments (" + argCount + ") in " + sqlized);
        try (PreparedStatement statement = connection.prepareStatement(sqlized)) {
            setupParms(statement, parms);
            try {
                return new Response<>(preparedStatementUser.go(statement));
            } catch (SQLException t) {
                return new Response<>(t);
            }
        }
    }

    /**
     * Use a prepared statement.
     *
     * @param <T> Type of return value from user of connection.
     * @param preparedStatementUser Instance of PreparedStatementUser, usually as a lambda expression.
     * @param connection Database connection.
     * @param query SQL query, with optional parametric indicators (usually ?)
     * @param parms Object[] of parameter arguments.
     * @return A value of type T as a result of using a PreparedStatement.
     * @throws SQLException Error.
     */
    public <T> T usePreparedStatement(PreparedStatementUser<T> preparedStatementUser, Connection connection, String query, Object... parms) throws SQLException {
        var result = processPreparedStatement(preparedStatementUser, connection, query, parms);
        if (result.error != null)
            throw (SQLException)result.error;
        return result.value;
    }

    /**
     * Issue a parametric update query with '?' substitutions.
     *
     * @param connection Database connection.
     * @param query SQL update query text.
     * @param parms Parameter arguments.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean update(Connection connection, String query, Object... parms) throws SQLException {
        return usePreparedStatement(PreparedStatement::execute, connection, query, parms);
    }

    /**
     * Issue a parametric update query with '?' substitutions.
     *
     * @param query SQL update query text.
     * @param parms Parameter arguments.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean update(String query, Object... parms) throws SQLException {
        return useConnection(conn -> update(conn, query, parms));
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param connection Database connection.
     * @param query SELECT query text.
     * @param columnName Column name from which to retrieve first row's value.
     * @param parms Parameter arguments.
     * @return Value of first row in columnName.
     * @throws SQLException Error.
     */
    public Optional<?> valueOf(Connection connection, String query, String columnName, Object... parms) throws SQLException {
        return query(connection, query, rs -> {
            try {
                if (rs.next())
                    return new Response<>(Optional.ofNullable(rs.getObject(columnName)));
                return new Response<>(Optional.empty());
            } catch (SQLException sqe) {
                return new Response<>(sqe);
            }
        }, parms);
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param query SELECT query.
     * @param columnName Column name from which to retrieve first row's value.
     * @param parms Parameter arguments.
     * @return Value of first row in columnName.
     * @throws SQLException Error.
     */
    public Optional<?> valueOf(String query, String columnName, Object... parms) throws SQLException {
        return useConnection(conn -> valueOf(conn, query, columnName, parms));
    }

    /**
     * Obtain a lambda to generate a new Tuple-derived class from a ResultSet.
     *
     * @param codeDirectory Directory into which generated class (both source and .class) will be placed.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleClassName Name for new tuple class.
     * @param customisations Customisations for specific DBMS types.
     * @return - lambda which will generate the class given a ResultSet.
     */
    public static ResultSetReceiver<Result> newResultSetGeneratesTupleClass(String codeDirectory, String packageSpec, String tupleClassName, Customisations customisations) {
        return resultSet -> {
            try {
                ResultSetToTuple.createTuple(codeDirectory, packageSpec, tupleClassName, resultSet, customisations);
            } catch (Throwable e) {
                return new Response<>(Result.ERROR(e));
            }
            return new Response<>(Result.OK);
        };
    }

    /**
     * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param connection Connection to database, usually obtained via a Transaction.
     * @param codeDirectory Directory in which compiled Tuple-derived source and .class will be generated
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleClassName Desired name of Tuple-derived class.
     * @param query Query to be evaluated.
     * @return Result of code generation.
     * @throws SQLException Error.
     */
    public Result createTupleFromQueryAll(Connection connection, String codeDirectory, String packageSpec, String tupleClassName, String query) throws SQLException {
        var resultSetReceiver = newResultSetGeneratesTupleClass(codeDirectory, packageSpec, tupleClassName, customisations);
        return queryAll(connection, query, resultSetReceiver);
    }

    /**
     * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param codeDirectory Directory in which compiled Tuple-derived source and .class will be generated.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleClassName Desired name of Tuple-derived class.
     * @param query Query to be evaluated.
     * @return Result of code generation.
     * @throws SQLException Error.
     */
    public Result createTupleFromQueryAll(String codeDirectory, String packageSpec, String tupleClassName, String query) throws SQLException {
        return (new Transaction(connection -> createTupleFromQueryAll(connection, codeDirectory, packageSpec, tupleClassName, query))).getResult();
    }

    /**
     * Use a parametric SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param connection Connection to database, usually obtained via a Transaction.
     * @param codeDirectory Directory in which compiled Tuple-derived source and .class will be generated.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleClassName Desired name of Tuple-derived class.
     * @param query Query to be evaluated.
     * @param parms Parameter arguments which positionally match to '?' in the query.
     * @return Result of code generation.
     * @throws SQLException Error.
     */
    public Result createTupleFromQuery(Connection connection, String codeDirectory, String packageSpec, String tupleClassName, String query, Object... parms) throws SQLException {
        var resultSetReceiver = newResultSetGeneratesTupleClass(codeDirectory, packageSpec, tupleClassName, customisations);
        return query(connection, query, resultSetReceiver, parms);
    }

    /**
     * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param codeDirectory Directory in which compiled Tuple-derived source and .class will be generated.
     * @param packageSpec The package, in dotted notation, to which the Tuple belongs.
     * @param tupleClassName Desired name of Tuple-derived class.
     * @param query Query to be evaluated.
     * @param parms Parameter arguments which positionally match to '?' in the query
     * @return Result of code generation.
     * @throws SQLException Error.
     */
    public Result createTupleFromQuery(String codeDirectory, String packageSpec, String tupleClassName, String query, Object... parms) throws SQLException {
        return (new Transaction(connection -> createTupleFromQuery(connection, codeDirectory, packageSpec, tupleClassName, query, parms))).getResult();
    }

    /**
     * Obtain a lambda that converts a ResultSet to a Stream&lt;T&gt; where T extends Tuple.
     *
     * @param <T> T extends Tuple.
     * @param tupleClass The stream will be of instances of tupleClass.
     * @return A ResultSetReceiver&lt;Stream&lt;T&gt;&gt;.
     */
    public static <T extends Tuple> ResultSetReceiver<Stream<T>> newResultSetToStream(Class<T> tupleClass) {
        return result -> {
            try {
                return new Response<>(ResultSetToTuple.toStream(result, tupleClass));
            } catch (Throwable e) {
                return new Response<>(new FatalException("ResultSet to Stream conversion failed in newResultSetToStream.", e));
            }
        };
    }

    /**
     * Obtain a lambda that converts a ResultSet to a Stream&lt;T&gt; where T extends Tuple,
     * and each Tuple is configured for a future update.
     *
     * @param <T> T extends Tuple.
     * @param tupleClass The stream will be of instances of tupleClass.
     * @return A ResultSetReceiver&lt;Stream&lt;T&gt;&gt; where the stream of tuples will have
     *         backup() invoked for each instance.
     */
    public static <T extends Tuple> ResultSetReceiver<Stream<T>> newResultSetToStreamForUpdate(Class<T> tupleClass) {
        return result -> {
            try {
                return new Response<>(ResultSetToTuple.toStreamForUpdate(result, tupleClass));
            } catch (Throwable e) {
                return new Response<>(new FatalException("ResultSet to Stream conversion failed in newResultSetToStreamForUpdate.", e));
            }
        };
    }

    /**
     * Issue a SELECT query, process it, and return the result
     *
     * @param <T> Return type
     * @param connection Database connection.
     * @param query SQL query.
     * @param receiver ResultSet receiver lambda.
     * @return Return value.
     * @throws SQLException Error.
     */
    public <T> T queryAll(Connection connection, String query, ResultSetReceiver<T> receiver) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            var sqlized = replaceTableNames(query);
            distributeSQLEvent("queryAll: ", sqlized);
            try (ResultSet rs = statement.executeQuery(sqlized)) {
                var response = receiver.go(rs);
                if (response.isError())
                    throw new SQLException("Failure inside ResultSetReceiver in queryAll.", response.error);
                return response.value;
            }
        }
    }

    /**
     * Issue a SELECT query, process it, and return the result
     *
     * @param <T> Return type.
     * @param query SQL query.
     * @param receiver ResultSet receiver lambda.
     * @return Return value.
     * @throws SQLException Error.
     */
    public <T> T queryAll(String query, ResultSetReceiver<T> receiver) throws SQLException {
        return useConnection(conn -> queryAll(conn, query, receiver));
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param connection Connection to database, typically obtained via a Transaction.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAll(Connection connection, String query, Class<T> tupleClass) throws SQLException {
        return queryAll(connection, query, newResultSetToStream(tupleClass));
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param connection Database connection, typically obtained via a Transaction.
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAll(Connection connection, Query<T> query) throws SQLException {
        return queryAll(connection, query.getQueryText(), query.getTupleClass());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param query SQL query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAll(String query, Class<T> tupleClass) throws SQLException {
        return queryAll(query, newResultSetToStream(tupleClass));
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAll(Query<T> query) throws SQLException {
        return queryAll(query.getQueryText(), query.getTupleClass());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param connection Database connection, typically obtained via a Transaction
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAllForUpdate(Connection connection, String query, Class<T> tupleClass) throws SQLException {
        return queryAll(connection, query, newResultSetToStreamForUpdate(tupleClass));
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param connection Connection to database, typically obtained via a Transaction
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAllForUpdate(Connection connection, Query<T> query) throws SQLException {
        return queryAllForUpdate(connection, query.getQueryText(), query.getTupleClass());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T>  T extends Tuple.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAllForUpdate(String query, Class<T> tupleClass) throws SQLException {
        return queryAll(query, newResultSetToStreamForUpdate(tupleClass));
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAllForUpdate(Query<T> query) throws SQLException {
        return queryAllForUpdate(query.getQueryText(), query.getTupleClass());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param connection Database connection, typically obtained via a Transaction.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @param parms Parameter argument list.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryForUpdate(Connection connection, String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return query(connection, query, newResultSetToStreamForUpdate(tupleClass), parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param connection Database connection, typically obtained via a Transaction.
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryForUpdate(Connection connection, Query<T> query) throws SQLException {
        return queryForUpdate(connection, query.getQueryText(), query.getTupleClass(), query.getArguments());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T>  T extends Tuple.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @param parms Parameter argument list.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryForUpdate(String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return query(query, newResultSetToStreamForUpdate(tupleClass), parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryForUpdate(Query<T> query) throws SQLException {
        return queryForUpdate(query.getQueryText(), query.getTupleClass(), query.getArguments());
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions, process it, and return the result
     *
     * @param <T> Return type.
     * @param connection Database connection.
     * @param query SQL SELECT query text.
     * @param receiver ResultSet receiver lambda.
     * @param parms Parameter arguments.
     * @return Return value.
     * @throws SQLException Error.
     */
    public <T> T query(Connection connection, String query, ResultSetReceiver<T> receiver, Object... parms) throws SQLException {
        return usePreparedStatement(statement -> {
            try (ResultSet rs = statement.executeQuery()) {
                var response = receiver.go(rs);
                if (response.isError())
                    throw new SQLException("Failure inside ResultSetReceiver in query.", response.error);
                return response.value;
            }
        }, connection, query, parms);
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions, process it, and return the result
     *
     * @param <T> Return type.
     * @param query SELECT query text.
     * @param receiver ResultSet receiver lambda.
     * @param parms Parameter arguments.
     * @return Return value.
     * @throws SQLException Error.
     */
    public <T> T query(String query, ResultSetReceiver<T> receiver, Object... parms) throws SQLException {
        return useConnection(conn -> query(conn, query, receiver, parms));
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param connection Database connection, typically obtained via a Transaction.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @param parms Parameter argument list.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> query(Connection connection, String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return query(connection, query, newResultSetToStream(tupleClass), parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param connection Database connection, typically obtained via a Transaction
     * @param query A Query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> query(Connection connection, Query<T> query) throws SQLException {
        return query(connection, query.getQueryText(), query.getTupleClass(), query.getArguments());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @param parms Parameter argument list.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> query(String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return query(query, newResultSetToStream(tupleClass), parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T>  T extends Tuple.
     * @param query A Query
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> query(Query<T> query) throws SQLException {
        return query(query.getQueryText(), query.getTupleClass(), query.getArguments());
    }

    /**
     * Issue an update query.
     *
     * @param update Update query specification.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean updateAll(Update update) throws SQLException {
        return updateAll(update.getQueryText());
    }

    /**
     * Issue an update query.
     *
     * @param connection Database connection.
     * @param update Update query specification.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean updateAll(Connection connection, Update update) throws SQLException {
        return updateAll(connection, update.getQueryText());
    }

    /**
     * Issue an update query.
     *
     * @param update Parametric update query specification.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean update(Update update) throws SQLException {
        return update(update.getQueryText(), update.getArguments());
    }

    /**
     * Issue an update query.
     *
     * @param connection Database connection.
     * @param update Parametric update query specification.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean update(Connection connection, Update update) throws SQLException {
        return update(connection, update.getQueryText(), update.getArguments());
    }

    /**
     * Given multiple argument arrays, combine them into one unified argument list for passing to a parametrised query's "Object ... parms", above.
     *
     * @param parms Parameter arguments.
     * @return Object array of parameter arguments.
     */
    public static Object[] allArguments(Object... parms) {
        Vector<Object> newArgs = new Vector<>();
        for (Object parm : parms)
            if (parm instanceof Object[])
                Collections.addAll(newArgs, (Object[]) parm);
            else
                newArgs.add(parm);
        return newArgs.toArray();
    }

    /**
     * Used to define lambda expressions for transactional processing.
     */
    @FunctionalInterface
    public interface TransactionRunner {
        /**
         * Run an operation in a Transaction.
         *
         * @param connection Connection to database.
         * @return Result of operation.
         * @throws SQLException thrown if operation fails.
         */
        Result run(Connection connection) throws SQLException;
    }

    /**
     * Encapsulates a transaction.
     */
    public class Transaction {

        private Result result;

        /**
         * Encapsulate a transaction.
         *
         * @param transactionRunner A lambda defining code to run within a transaction. If it throws an error or returns false, the transaction is rolled back.
         * @throws SQLException Error getting connection.
         */
        public Transaction(TransactionRunner transactionRunner) throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    result = transactionRunner.run(connection);
                } catch (Throwable t) {
                    connection.rollback();
                    result = Result.ERROR(t);
                    return;
                }
                if (result.isOk())
                    connection.commit();
                else
                    connection.rollback();
            }
        }

        /**
         * Obtain transaction execution result.
         *
         * @return Result of transaction execution.
         */
        public Result getResult() {
            return result;
        }
    }

    /**
     * Execute some code in a transaction.
     *
     * @param transactionRunner The lambda specifying code to be run.
     * @return The transaction execution result.
     * @throws SQLException Error.
     */
    public Result processTransaction(TransactionRunner transactionRunner) throws SQLException {
        var transaction = new Transaction(transactionRunner);
        return transaction.getResult();
    }

    /**
     * Execute some code in a transaction.
     *
     * @param transactionRunner The lambda specifying code to be run.
     * @return Result.
     * @throws SQLException Error.
     */
    public Result useTransaction(TransactionRunner transactionRunner) throws SQLException {
        return processTransaction(transactionRunner);
    }

    /**
     * Used to define lambda expressions for more ergonomic transaction processing.
     */
    @FunctionalInterface
    public interface XactGo {
        /**
         * Run the transaction
         *
         * @param tcw Transaction wrapper.
         * @return Result of transaction.
         * @throws SQLException thrown if operation fails.
         */
        Result go(Xact tcw) throws SQLException;
    }

    /**
     * Run one or more database operations in a transaction wrapped with Xact for syntactic convenience.
     *
     * @param transactionRunner The lambda defining one or more database operations
     * @return Result.
     * @throws SQLException Error.
     */
    public Result transact(XactGo transactionRunner) throws SQLException {
        return useTransaction(conn -> transactionRunner.go(new Xact(Database.this, conn)));
    }

    // Primary key cache.
    private final Map<String, String[]> keyCache = new HashMap<>();

    /**
     * Get primary key for a given table.
     *
     * @param tableName Table name.
     * @param connection Connection to database; typically obtained via a Transaction.
     * @return Array of column names comprising the primary key.
     * @throws SQLException Error.
     */
    public String[] getKeyColumnNamesFor(Connection connection, String tableName) throws SQLException {
        var keyColumnNamesArray = keyCache.get(tableName);
        if (keyColumnNamesArray == null) {
            var metadata = connection.getMetaData();
            var realTableName = replaceTableNames(tableName);
            var keys = metadata.getPrimaryKeys(null, null, realTableName);
            var keyColumnNames = new LinkedList<String>();
            while (keys.next())
                keyColumnNames.add(keys.getString("COLUMN_NAME"));
            keyColumnNamesArray = keyColumnNames.toArray(new String[0]);
            keyCache.put(tableName, keyColumnNamesArray);
        }
        return keyColumnNamesArray;
    }

    /**
     * Get primary key for a given table.
     *
     * @param tableName Table name.
     * @return Array of column names comprising the primary key.
     * @throws SQLException Error.
     */
    public String[] getKeyColumnNamesFor(String tableName) throws SQLException {
        return useConnection(connection -> getKeyColumnNamesFor(connection, tableName));
    }

    /**
     * If the String argument is null or an empty string, return null.
     *
     * @param str Input string or null.
     * @return String or null
     */
    public static String emptyToNull(String str) {
        return (str == null || str.trim().length() == 0) ? null : str;
    }

    /**
     * If the String argument is null or an empty string, return a specified replacement string.
     *
     * @param str Input string or null.
     * @param replacement A replacement string.
     * @return A string.
     */
    public static String nullTo(String str, String replacement) {
        return (emptyToNull(str) == null) ? replacement : str;
    }

    /**
     * If the String argument is null or an empty string, return an empty string.
     *
     * @param str Input string or null.
     * @return A string.
     */
    public static String nullToEmptyString(String str) {
        return nullTo(str, "");
    }

}
