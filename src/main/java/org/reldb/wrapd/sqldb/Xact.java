package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.response.Response;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.sqldb.Database.PreparedStatementUser;
import org.reldb.wrapd.sqldb.Database.ResultSetReceiver;
import org.reldb.wrapd.tuples.Tuple;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

/**
 * Transaction wrapper that allows database query and update operations to be invoked transactionally in an ergonomic fashion.
 */
public class Xact {

    /** Database upon which this transaction will run. */
    protected final Database database;

    /** Connection to database this transaction is running in. */
    protected final Connection connection;

    /**
     * Create a Xact to run a transaction.
     *
     * @param database The Database upon which this transaction will run.
     * @param connection The connection this transaction is running in.
     */
    Xact(Database database, Connection connection) {
        this.database = database;
        this.connection = connection;
    }

    /**
     * Issue an update query.
     *
     * @param sqlStatement String SQL query.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean updateAll(String sqlStatement) throws SQLException {
        return database.updateAll(connection, sqlStatement);
    }

    /**
     * Issue a SELECT query and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param query SELECT query.
     * @param columnName Column name from which to retrieve first row's value.
     * @return Value of first row in columnName.
     * @throws SQLException Error.
     */
    public Object valueOfAll(String query, String columnName) throws SQLException {
        return database.valueOfAll(connection, query, columnName);
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
    public Object valueOf(String query, String columnName, Object... parms) throws SQLException {
        return database.valueOf(connection, query, columnName, parms);
    }

    /**
     * Issue a SELECT query, process it, and return the result
     *
     * @param <T> Return type.
     * @param query Query text.
     * @param receiver ResultSet receiver lambda.
     * @return Return value.
     * @throws SQLException Error.
     */
    public <T> T queryAll(String query, ResultSetReceiver<T> receiver) throws SQLException {
        return database.queryAll(connection, query, receiver);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T>  T extends Tuple.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAll(String query, Class<T> tupleClass) throws SQLException {
        return database.queryAll(connection, query, tupleClass);
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
        return database.queryAll(connection, query.getQueryText(), query.getTupleClass());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param query Query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query.
     * @return Stream&lt;T&gt; Result stream.
     * @throws SQLException Error.
     */
    public <T extends Tuple> Stream<T> queryAllForUpdate(String query, Class<T> tupleClass) throws SQLException {
        return database.queryAllForUpdate(connection, query, tupleClass);
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
        return database.queryAllForUpdate(connection, query.getQueryText(), query.getTupleClass());
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions, process it, and return the result
     *
     * @param <T> Return type
     * @param query SQL query.
     * @param receiver ResultSet receiver lambda.
     * @param parms Parameter arguments.
     * @return Return value.
     * @throws SQLException Error.
     */
    public <T> T query(String query, ResultSetReceiver<T> receiver, Object... parms) throws SQLException {
        return database.query(connection, query, receiver, parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T> T extends Tuple.
     * @param query SQL query string
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @param parms Parameter arguments to parametric query.
     * @return Stream&lt;T&gt; Result stream
     * @throws SQLException Error
     */
    public <T extends Tuple> Stream<T> query(String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return database.query(connection, query, tupleClass, parms);
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
        return database.query(connection, query.getQueryText(), query.getTupleClass(), query.getArguments());
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T> T extends Tuple.
     * @param query SQL query string.
     * @param tupleClass Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @param parms Parameter arguments to parametric query.
     * @return Stream&lt;T&gt; Result stream
     * @throws SQLException Error
     */
    public <T extends Tuple> Stream<T> queryForUpdate(String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return database.queryForUpdate(connection, query, tupleClass, parms);
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
        return database.queryForUpdate(connection, query.getQueryText(), query.getTupleClass(), query.getArguments());
    }

    /**
     * Insert specified Tuple.
     *
     * @param tuple Tuple to insert.
     * @param tableName Table name.
     * @return Should return false.
     * @throws SQLException Failure.
     */
    public boolean insert(Tuple tuple, String tableName) throws SQLException {
        return tuple.insert(database, connection, tableName);
    }

    /**
     * Issue a parametric update query with '?' substitutions.
     *
     * @param query SQL query.
     * @param parms Parameter arguments.
     * @return True if a ResultSet is returned, false otherwise.
     * @throws SQLException Error.
     */
    public boolean update(String query, Object... parms) throws SQLException {
        return database.update(connection, query, parms);
    }

    /**
     * Update specified tuple.
     *
     * @param tuple Tuple to update.
     * @param tableName Table name.
     * @return Should return false.
     * @throws SQLException Failure.
     */
    public boolean update(Tuple tuple, String tableName) throws SQLException {
        return tuple.update(database, connection, tableName);
    }

    /**
     * Use a parametric SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param codeDirectory Directory in which compiled Tuple-derived source and .class will be generated.
     * @param tupleClassName Name of desired Tuple-derived class.
     * @param query Query to be evaluated.
     * @param parms Parameter arguments which positionally match to '?' in the query.
     * @return Result of code generation.
     * @throws SQLException Error.
     */
    public Result createTupleFromQuery(String codeDirectory, String tupleClassName, String query, Object... parms) throws SQLException {
        return database.createTupleFromQuery(connection, codeDirectory, tupleClassName, query, parms);
    }

    /**
     * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param codeDirectory Directory in which compiled Tuple-derived source and .class will be generated.
     * @param tupleClassName Name of desired Tuple-derived class.
     * @param query Query to be evaluated.
     * @return Result of code generation.
     * @throws SQLException Error.
     */
    public Result createTupleFromQueryAll(String codeDirectory, String tupleClassName, String query) throws SQLException {
        return database.createTupleFromQueryAll(connection, codeDirectory, tupleClassName, query);
    }

    /**
     * Get primary key for a given table.
     *
     * @param tableName Table name.
     * @return Array of column names comprising the primary key.
     * @throws SQLException Error.
     */
    public String[] getKeyColumnNamesFor(String tableName) throws SQLException {
        return database.getKeyColumnNamesFor(connection, tableName);
    }

    /**
     * Use a prepared statement.
     *
     * @param <T> The type of return value from use of connection.
     * @param preparedStatementUser Instance of PreparedStatementUser, usually as a lambda expression.
     * @param query SQL text.
     * @param parms Parameter arguments to prepared statement.
     * @return A PreparedStatementUseResult&lt;T&gt; containing either a T (indicating success) or a SQLException.
     * @throws SQLException Error
     */
    public <T> Response<T> processPreparedStatement(PreparedStatementUser<T> preparedStatementUser, String query, Object... parms) throws SQLException {
        return database.processPreparedStatement(preparedStatementUser, connection, query, parms);
    }

    /**
     * Use a prepared statement.
     *
     * @param <T> Type of return value from user of connection.
     * @param preparedStatementUser Instance of PreparedStatementUser, usually as a lambda expression.
     * @param query SQL text.
     * @param parms Parameter arguments to prepared statement.
     * @return A value of type T as a result of using a PreparedStatement.
     * @throws SQLException Error
     */
    public <T> T usePreparedStatement(PreparedStatementUser<T> preparedStatementUser, String query, Object... parms) throws SQLException {
        return database.usePreparedStatement(preparedStatementUser, connection, query, parms);
    }

}
