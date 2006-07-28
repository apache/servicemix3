/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface JDBCAdapter {

    public void doCreateTables(Connection connection) throws SQLException, IOException;
    
    public void doDropTables(Connection connection) throws SQLException, IOException;
    
    public byte[] doLoadData(Connection connection, String id) throws SQLException, IOException;
    
    public byte[][] doLoadData(Connection connection, String[] ids) throws SQLException, IOException;
    
    public void doStoreData(Connection connection, String id, byte[] data) throws SQLException, IOException;
    
    public void doUpdateData(Connection connection, String id, byte[] data) throws SQLException, IOException;
    
    public void doRemoveData(Connection connection, String id) throws SQLException, IOException;
    
    public void doRemoveData(Connection connection, String[] ids) throws SQLException, IOException;
    
    public int doGetCount(Connection connection) throws SQLException, IOException;
    
    public String[] doGetIds(Connection connection) throws SQLException, IOException;
    
    public String[] doGetIds(Connection connection, int fromIndex, int toIndex) throws SQLException, IOException;
    
    public Statements getStatements();
    
    public void setStatements(Statements statements);
    
}
