package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.sqldb.Database.PreparedStatementUseResult;
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

    protected final Database database;
    protected final Connection connection;

    Xact(Database database, Connection connection) {
        this.database = database;
        this.connection = connection;
    }

    /**
     * Issue a SELECT query, process it, and return the result
     *
     * @param <T>      return type
     * @param query    - query
     * @param receiver - result set receiver lambda
     * @return return value
     * @throws SQLException - Error
     */
    public <T> T queryAll(String query, ResultSetReceiver<T> receiver) throws SQLException {
        return database.queryAll(connection, query, receiver);
    }

    /**
     * Issue an update query.
     *
     * @param sqlStatement - String SQL query
     * @return true if a ResultSet is returned, false otherwise
     * @throws SQLException - Error
     */
    public boolean updateAll(String sqlStatement) throws SQLException {
        return database.updateAll(connection, sqlStatement);
    }

    /**
     * Issue a SELECT query and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param query      - SELECT query
     * @param columnName - column name from which to retrieve first row's value
     * @return - value of first row in columnName
     * @throws SQLException - Error
     */
    public Object valueOfAll(String query, String columnName) throws SQLException {
        return database.valueOfAll(connection, query, columnName);
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions, process it, and return the result
     *
     * @param <T>      return type
     * @param query    - query
     * @param receiver - result set receiver lambda
     * @param parms    - parameters
     * @return return value
     * @throws SQLException - Error
     */
    public <T> T query(String query, ResultSetReceiver<T> receiver, Object... parms) throws SQLException {
        return database.query(connection, query, receiver, parms);
    }

    /**
     * Issue a parametric update query with '?' substitutions.
     *
     * @param query - String SQL query
     * @param parms - parameters
     * @return true if a ResultSet is returned, false otherwise
     * @throws SQLException - Error
     */
    public boolean update(String query, Object... parms) throws SQLException {
        return database.update(connection, query, parms);
    }

    /**
     * Issue a parametric SELECT query with '?' substitutions and obtain a value for the first row in a specified column name. Intended to obtain a single value.
     *
     * @param query      - SELECT query
     * @param columnName - column name from which to retrieve first row's value
     * @param parms      - parameters
     * @return - value of first row in columnName
     * @throws SQLException - Error
     */
    public Object valueOf(String query, String columnName, Object... parms) throws SQLException {
        return database.valueOf(connection, query, columnName, parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T>        - T extends Tuple.
     * @param query      - query string
     * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @return Stream<T> - result stream
     * @throws SQLException - Error
     */
    public <T extends Tuple> Stream<T> queryAll(String query, Class<T> tupleClass) throws SQLException {
        return database.queryAll(connection, query, tupleClass);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T>        - T extends Tuple.
     * @param query      - query string
     * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @return Stream<T> - result stream
     * @throws SQLException - Error
     */
    public <T extends Tuple> Stream<T> queryAllForUpdate(String query, Class<T> tupleClass) throws SQLException {
        return database.queryAllForUpdate(connection, query, tupleClass);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation.
     *
     * @param <T>        - T extends Tuple.
     * @param query      - query string
     * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @return Stream<T> - result stream
     * @throws SQLException - Error
     */
    public <T extends Tuple> Stream<T> query(String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return database.query(connection, query, tupleClass, parms);
    }

    /**
     * Obtain a stream of Tuple derivatives from a query evaluation for possible update.
     *
     * @param <T>        - T extends Tuple.
     * @param query      - query string
     * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
     * @return Stream<T> - result stream
     * @throws SQLException - Error
     */
    public <T extends Tuple> Stream<T> queryForUpdate(String query, Class<T> tupleClass, Object... parms) throws SQLException {
        return database.queryForUpdate(connection, query, tupleClass, parms);
    }

    /**
     * Insert specified Tuple.
     *
     * @param tuple     - tuple to insert
     * @param tableName - table name
     * @return - should return false
     * @throws SQLException on failure
     */
    public boolean insert(Tuple tuple, String tableName) throws SQLException {
        return tuple.insert(database, connection, tableName);
    }

    /**
     * Update specified tuple.
     *
     * @param tuple     - tuple to update
     * @param tableName - table name
     * @return - should return false
     * @throws SQLException on failure
     */
    public boolean update(Tuple tuple, String tableName) throws SQLException {
        return tuple.update(database, connection, tableName);
    }

    /**
     * Use a parametric SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param codeDirectory  - directory in which compiled Tuple-derived source and .class will be generated
     * @param tupleClassName - desired Tuple-derived class name
     * @param query          - String - query to be evaluated
     * @param parms          - parameters which positionally match to '?' in the query
     * @return - true if Tuple-derived class has been created and compiled; false otherwise
     * @throws SQLException - Error
     */
    public boolean createTupleFromQuery(String codeDirectory, String tupleClassName, String query, Object... parms) throws SQLException {
        return database.createTupleFromQuery(connection, codeDirectory, tupleClassName, query, parms);
    }

    /**
     * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
     *
     * @param codeDirectory  - directory in which compiled Tuple-derived source and .class will be generated
     * @param tupleClassName - desired Tuple-derived class name
     * @param query          - String - query to be evaluated
     * @return - true if Tuple-derived class has been created and compiled; false otherwise
     * @throws SQLException - Error
     */
    public boolean createTupleFromQueryAll(String codeDirectory, String tupleClassName, String query) throws SQLException {
        return database.createTupleFromQueryAll(connection, codeDirectory, tupleClassName, query);
    }

    /**
     * Get primary key for a given table.
     *
     * @param tableName - table name
     * @return - array of column names comprising the primary key
     * @throws SQLException - Error
     */
    public String[] getKeyColumnNamesFor(String tableName) throws SQLException {
        return database.getKeyColumnNamesFor(connection, tableName);
    }

    /**
     * Use a prepared statement.
     *
     * @param <T>                   type of return value from use of connection.
     * @param preparedStatementUser - Instance of PreparedStatementUser, usually as a lambda expression.
     * @return A PreparedStatementUseResult<T> containing either a T (indicating success) or a SQLException.
     * @throws SQLException - Error
     */
    public <T> PreparedStatementUseResult<T> processPreparedStatement(PreparedStatementUser<T> preparedStatementUser, String query, Object... parms) throws SQLException {
        return database.processPreparedStatement(preparedStatementUser, connection, query, parms);
    }

    /**
     * Use a prepared statement.
     *
     * @param <T>                   type of return value from user of connection.
     * @param preparedStatementUser - Instance of PreparedStatementUser, usually as a lambda expression.
     * @return A value of type T as a result of using a PreparedStatement.
     * @throws SQLException - Error
     */
    public <T> T usePreparedStatement(PreparedStatementUser<T> preparedStatementUser, String query, Object... parms) throws SQLException {
        return database.usePreparedStatement(preparedStatementUser, connection, query, parms);
    }

}
