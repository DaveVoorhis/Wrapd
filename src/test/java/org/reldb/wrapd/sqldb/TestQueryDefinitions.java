package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class TestQueryDefinitions {

    private String testStagePrompt = "[TSET]";

    // TODO parametrise the following tests

    @Test
    public void testCodeThatUsesGeneratedTupleMySQL() throws IOException, ClassNotFoundException, SQLException, QueryDefiner.QueryDefinerException {
        var pkg = org.reldb.wrapd.sqldb.mysql.Configuration.dbPackage;
        var name = org.reldb.wrapd.sqldb.mysql.Configuration.dbName;
        var db = org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase(testStagePrompt);
        new QueriesHelper(pkg, name).test(db);
    }

    @Test
    public void testCodeThatUsesGeneratedTuplePostgreSQL() throws IOException, ClassNotFoundException, SQLException, QueryDefiner.QueryDefinerException {
        var pkg = org.reldb.wrapd.sqldb.postgresql.Configuration.dbPackage;
        var name = org.reldb.wrapd.sqldb.postgresql.Configuration.dbName;
        var db = org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase(testStagePrompt);
        new QueriesHelper(pkg, name).test(db);
    }

    @Test
    public void testCodeThatUsesGeneratedTupleSQLite() throws IOException, ClassNotFoundException, SQLException, QueryDefiner.QueryDefinerException {
        var pkg = org.reldb.wrapd.sqldb.sqlite.Configuration.dbPackage;
        var name = org.reldb.wrapd.sqldb.sqlite.Configuration.dbName;
        var db = org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase(testStagePrompt);
        new QueriesHelper(pkg, name).test(db);
    }

}
