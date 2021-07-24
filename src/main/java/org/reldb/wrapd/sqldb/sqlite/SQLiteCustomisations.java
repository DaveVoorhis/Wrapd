package org.reldb.wrapd.sqldb.sqlite;

import org.reldb.wrapd.sqldb.Customisations;

import java.sql.Date;

/**
 * Customisations required for SQLite.
 */
public class SQLiteCustomisations implements Customisations {
    @Override
    public String getSpecificColumnClass(String columnSQLType) {
        switch (columnSQLType) {
            case "BOOLEAN": return "java.lang.Boolean";
            case "TINYINT":
            case "SMALLINT":
            case "INT2":
            case "INT":
            case "INTEGER":
            case "MEDIUMINT":
                return "java.lang.Integer";
            case "BIGINT":
            case "INT8":
            case "UNSIGNED BIG INT":
                return "java.lang.Long";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
                return "java.util.Date";
            case "DECIMAL":
            case "DOUBLE":
            case "DOUBLE PRECISION":
            case "NUMERIC":
            case "REAL":
            case "FLOAT":
                return "java.lang.Double";
            case "CHARACTER":
            case "NCHAR":
            case "NATIVE CHARACTER":
            case "CHAR":
            case "VARCHAR":
            case "VARYING CHARACTER":
            case "NVARCHAR":
            case "TEXT":
                return "java.lang.String";
            default: return "java.lang.Object";
        }
    }

    @Override
    public Object getSpecificColumnValue(Object retrievedValue, String columnSQLType) {
        switch (getSpecificColumnClass(columnSQLType)) {
            case "java.lang.Boolean": return Boolean.parseBoolean(retrievedValue.toString());
            case "java.lang.Integer": return Integer.parseInt(retrievedValue.toString());
            case "java.lang.Long": return Long.parseLong(retrievedValue.toString());
            case "java.util.Date": return Date.valueOf(retrievedValue.toString());
            case "java.lang.Double": return Double.parseDouble(retrievedValue.toString());
            case "java.lang.String": return retrievedValue.toString();
            default:
                return retrievedValue;
        }
    }
}
