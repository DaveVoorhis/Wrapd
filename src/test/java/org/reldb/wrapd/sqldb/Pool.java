package org.reldb.wrapd.sqldb;

import com.mchange.v2.c3p0.DataSources;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Pool {
    private DataSource dataSource;

    public Pool(String dbURL, String dbUser, String dbPassword) throws SQLException {
        if (dbURL == null)
            throw new IllegalArgumentException("dbURL must not be null");

        Properties props = new Properties();
        if (dbUser != null)
            props.setProperty("user", dbUser);
        if (dbPassword != null)
            props.setProperty("password", dbPassword);

        DriverManager.getConnection(dbURL, props).close();
        var unpooledSource = DataSources.unpooledDataSource(dbURL, props);
        dataSource = DataSources.pooledDataSource(unpooledSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
