package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestSQLParameterConverter {

    @Test
    public void testNoParameters() {
        var sql = "select * from blah where x = 3";
        var converter = new SQLParameterConverter(sql);
        converter.process();
        assertTrue(converter.getParameterNames().isEmpty());
        assertEquals(sql, converter.getSQLText());
    }

    @Test
    public void testOneParameter() {
        var sql = "select * from blah where x = {parm}";
        var converter = new SQLParameterConverter(sql);
        converter.process();
        assertEquals("parm", converter.getParameterNames().get(0));
        assertEquals("select * from blah where x = ?", converter.getSQLText());
    }

    @Test
    public void testTwoParameters() {
        var sql = "select * from blah where x = {parm1} and y = {parm2}";
        var converter = new SQLParameterConverter(sql);
        converter.process();
        assertEquals("parm1", converter.getParameterNames().get(0));
        assertEquals("parm2", converter.getParameterNames().get(1));
        assertEquals(2, converter.getParameterNames().size());
        assertEquals("select * from blah where x = ? and y = ?", converter.getSQLText());
    }

    @Test
    public void testTwoParameterCollision() {
        var sql = "select * from blah where x = {parm2} and y = {parm2}";
        var converter = new SQLParameterConverter(sql);
        assertThrows(IllegalArgumentException.class, converter::process);
    }

    @Test
    public void testBadParm01() {
        var sql = "select * from blah where x = {parm1";
        var converter = new SQLParameterConverter(sql);
        assertThrows(IllegalArgumentException.class, converter::process);
    }

    @Test
    public void testBadParm02() {
        var sql = "select * from blah where x = {1parm1}";
        var converter = new SQLParameterConverter(sql);
        assertThrows(IllegalArgumentException.class, converter::process);
    }

    @Test
    public void testBadParm03() {
        var sql = "select * from blah where x = {para.meter1}";
        var converter = new SQLParameterConverter(sql);
        assertThrows(IllegalArgumentException.class, converter::process);
    }

}
