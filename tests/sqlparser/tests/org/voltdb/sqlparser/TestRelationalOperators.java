/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
/*
 * This file has been created by elves.  If you make changes to it,
 * the elves will become annoyed, will overwrite your changes with
 * whatever odd notions they have of what should
 * be here, and ignore your plaintive bleatings.  So, don't edit this file,
 * Unless you want your work to disappear.
 */
package org.voltdb.sqlparser;

import org.junit.Test;

import org.hsqldb_voltpatches.VoltXMLElement;
import org.hsqldb_voltpatches.HSQLInterface;
import org.voltdb.sqlparser.syntax.SQLKind;

import org.voltdb.sqlparser.assertions.semantics.VoltXMLElementAssert.IDTable;
import static org.voltdb.sqlparser.assertions.semantics.VoltXMLElementAssert.*;

public class TestRelationalOperators {
    HSQLInterface m_HSQLInterface = null;
    String        m_schema = null;
    public TestRelationalOperators() {
        m_HSQLInterface = HSQLInterface.loadHsqldb();
        String m_schema = "create table alpha ( id integer, beta integer );create table gamma ( id integer not null, zooba integer );create table fargle ( id integer not null, dooba integer )";
        try {
            m_HSQLInterface.processDDLStatementsUsingVoltSQLParser(m_schema, null);
        } catch (Exception ex) {
            System.err.printf("Error parsing ddl: %s\n", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestEq() throws Exception {
        String sql    = "select * from alpha where id = 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "table", "ALPHA")),
                    withChildNamed(8, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(9, "alias", "BETA"),
                        withAttribute(10, "column", "BETA"),
                        withIdAttribute(11, idTable),
                        withAttribute(12, "table", "ALPHA"))),
                withChildNamed(13, "parameters"),
                withChildNamed(14, "tablescans",
                    withChildNamed(15, "tablescan",
                        withAttribute(16, "jointype", "inner"),
                        withAttribute(17, "table", "ALPHA"),
                        withChildNamed(18, "joincond",
                            withChildNamed(19, "operation",
                                           "optype", "equal",
                                withIdAttribute(20, idTable),
                                withAttribute(21, "optype", "equal"),
                                withChildNamed(22, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(23, "alias", "ID"),
                                    withAttribute(24, "column", "ID"),
                                    withIdAttribute(25, idTable),
                                    withAttribute(26, "table", "ALPHA")),
                                withChildNamed(27, "value",
                                    withIdAttribute(28, idTable),
                                    withAttribute(29, "value", "0"),
                                    withAttribute(30, "valuetype", "INTEGER")))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestNotEq() throws Exception {
        String sql    = "select * from alpha where id != 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "table", "ALPHA")),
                    withChildNamed(8, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(9, "alias", "BETA"),
                        withAttribute(10, "column", "BETA"),
                        withIdAttribute(11, idTable),
                        withAttribute(12, "table", "ALPHA"))),
                withChildNamed(13, "parameters"),
                withChildNamed(14, "tablescans",
                    withChildNamed(15, "tablescan",
                        withAttribute(16, "jointype", "inner"),
                        withAttribute(17, "table", "ALPHA"),
                        withChildNamed(18, "joincond",
                            withChildNamed(19, "operation",
                                           "optype", "notequal",
                                withIdAttribute(20, idTable),
                                withAttribute(21, "optype", "notequal"),
                                withChildNamed(22, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(23, "alias", "ID"),
                                    withAttribute(24, "column", "ID"),
                                    withIdAttribute(25, idTable),
                                    withAttribute(26, "table", "ALPHA")),
                                withChildNamed(27, "value",
                                    withIdAttribute(28, idTable),
                                    withAttribute(29, "value", "0"),
                                    withAttribute(30, "valuetype", "INTEGER")))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestLessThan() throws Exception {
        String sql    = "select * from alpha where id < 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "table", "ALPHA")),
                    withChildNamed(8, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(9, "alias", "BETA"),
                        withAttribute(10, "column", "BETA"),
                        withIdAttribute(11, idTable),
                        withAttribute(12, "table", "ALPHA"))),
                withChildNamed(13, "parameters"),
                withChildNamed(14, "tablescans",
                    withChildNamed(15, "tablescan",
                        withAttribute(16, "jointype", "inner"),
                        withAttribute(17, "table", "ALPHA"),
                        withChildNamed(18, "joincond",
                            withChildNamed(19, "operation",
                                           "optype", "lessthan",
                                withIdAttribute(20, idTable),
                                withAttribute(21, "optype", "lessthan"),
                                withChildNamed(22, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(23, "alias", "ID"),
                                    withAttribute(24, "column", "ID"),
                                    withIdAttribute(25, idTable),
                                    withAttribute(26, "table", "ALPHA")),
                                withChildNamed(27, "value",
                                    withIdAttribute(28, idTable),
                                    withAttribute(29, "value", "0"),
                                    withAttribute(30, "valuetype", "INTEGER")))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestLessEqual() throws Exception {
        String sql    = "select * from alpha where id <= 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "table", "ALPHA")),
                    withChildNamed(8, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(9, "alias", "BETA"),
                        withAttribute(10, "column", "BETA"),
                        withIdAttribute(11, idTable),
                        withAttribute(12, "table", "ALPHA"))),
                withChildNamed(13, "parameters"),
                withChildNamed(14, "tablescans",
                    withChildNamed(15, "tablescan",
                        withAttribute(16, "jointype", "inner"),
                        withAttribute(17, "table", "ALPHA"),
                        withChildNamed(18, "joincond",
                            withChildNamed(19, "operation",
                                           "optype", "lessthanorequalto",
                                withIdAttribute(20, idTable),
                                withAttribute(21, "optype", "lessthanorequalto"),
                                withChildNamed(22, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(23, "alias", "ID"),
                                    withAttribute(24, "column", "ID"),
                                    withIdAttribute(25, idTable),
                                    withAttribute(26, "table", "ALPHA")),
                                withChildNamed(27, "value",
                                    withIdAttribute(28, idTable),
                                    withAttribute(29, "value", "0"),
                                    withAttribute(30, "valuetype", "INTEGER")))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestGreaterThan() throws Exception {
        String sql    = "select * from alpha where id > 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "table", "ALPHA")),
                    withChildNamed(8, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(9, "alias", "BETA"),
                        withAttribute(10, "column", "BETA"),
                        withIdAttribute(11, idTable),
                        withAttribute(12, "table", "ALPHA"))),
                withChildNamed(13, "parameters"),
                withChildNamed(14, "tablescans",
                    withChildNamed(15, "tablescan",
                        withAttribute(16, "jointype", "inner"),
                        withAttribute(17, "table", "ALPHA"),
                        withChildNamed(18, "joincond",
                            withChildNamed(19, "operation",
                                           "optype", "greaterthan",
                                withIdAttribute(20, idTable),
                                withAttribute(21, "optype", "greaterthan"),
                                withChildNamed(22, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(23, "alias", "ID"),
                                    withAttribute(24, "column", "ID"),
                                    withIdAttribute(25, idTable),
                                    withAttribute(26, "table", "ALPHA")),
                                withChildNamed(27, "value",
                                    withIdAttribute(28, idTable),
                                    withAttribute(29, "value", "0"),
                                    withAttribute(30, "valuetype", "INTEGER")))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void TestGreaterEqual() throws Exception {
        String sql    = "select * from alpha where id >= 0";
        IDTable idTable = new IDTable();
        VoltXMLElement element = m_HSQLInterface.getVoltXMLFromSQLUsingVoltSQLParser(sql, null, SQLKind.DQL);
        assertThat(element)
            .hasName(1, "select")
            .hasAllOf(
                withChildNamed(2, "columns",
                    withChildNamed(3, "columnref",
                                   "alias", "ID",
                                   "column", "ID",
                                   "table", "ALPHA",
                        withAttribute(4, "alias", "ID"),
                        withAttribute(5, "column", "ID"),
                        withIdAttribute(6, idTable),
                        withAttribute(7, "table", "ALPHA")),
                    withChildNamed(8, "columnref",
                                   "alias", "BETA",
                                   "column", "BETA",
                                   "table", "ALPHA",
                        withAttribute(9, "alias", "BETA"),
                        withAttribute(10, "column", "BETA"),
                        withIdAttribute(11, idTable),
                        withAttribute(12, "table", "ALPHA"))),
                withChildNamed(13, "parameters"),
                withChildNamed(14, "tablescans",
                    withChildNamed(15, "tablescan",
                        withAttribute(16, "jointype", "inner"),
                        withAttribute(17, "table", "ALPHA"),
                        withChildNamed(18, "joincond",
                            withChildNamed(19, "operation",
                                           "optype", "greaterthanorequalto",
                                withIdAttribute(20, idTable),
                                withAttribute(21, "optype", "greaterthanorequalto"),
                                withChildNamed(22, "columnref",
                                               "alias", "ID",
                                               "column", "ID",
                                               "table", "ALPHA",
                                    withAttribute(23, "alias", "ID"),
                                    withAttribute(24, "column", "ID"),
                                    withIdAttribute(25, idTable),
                                    withAttribute(26, "table", "ALPHA")),
                                withChildNamed(27, "value",
                                    withIdAttribute(28, idTable),
                                    withAttribute(29, "value", "0"),
                                    withAttribute(30, "valuetype", "INTEGER")))))));
    }
}