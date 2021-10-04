package org.reldb.wrapd.sqldb;

/**
 * An SQL update (typically INSERT, UPDATE, DELETE, or stored procedure invoking) query.
 *
 * Normally not used directly, but inherited.
 */
public class Update extends SQL {

    /**
     * Define an update query.
     *
     * @param queryText SQL text.
     * @param arguments Arguments to query.
     */
    protected Update(String queryText, Object... arguments) {
        super(queryText, arguments);
    }

    /**
     * Define an update query.
     *
     * @param queryText SQL text.
     */
    protected Update(String queryText) {
        this(queryText, new Object[] {});
    }

}
