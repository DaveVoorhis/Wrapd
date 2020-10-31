package org.reldb.wrapd.sqldb;

/**
 * Defines custom, DBMS-specific behaviour.
 */
public interface Customisations {
    /**
     * Some JDBC drivers return a general class type, like java.lang.Object,
     * for ResultSet::getMetaData().getColumnClassName(column). This method
     * specifies a more usefully specific class name to host the column value.
     *
     * @param columnSQLType - sql type name typically returned by ResultSet::getMetaData().getColumnTypeName(column)
     * @return - class name of class returned by getSpecificColumnValue(Object retrievedValue)
     */
    String getSpecificColumnClass(String columnSQLType);

    /**
     * As noted above, some JDBC drivers return a generic class for a column type.
     * Given a retrieved value of some overly-generic class -- probably java.lang.Object --
     * convert it to a more useful specific class type per getSpecificColumnClass() above.
     *
     * @param retrievedValue - value obtained via ResultSet::getObject(column)
     * @param columnSQLType - sql type name typically returned by ResultSet::getMetaData().getColumnTypeName(column)
     * @return - class name of class returned by getSpecificColumnValue(Object retrievedValue)
     */
    Object getSpecificColumnValue(Object retrievedValue, String columnSQLType);
}
