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
package org.apache.servicemix.store.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.servicemix.id.IdGenerator;
import org.apache.servicemix.jdbc.JDBCAdapter;
import org.apache.servicemix.jdbc.JDBCAdapterFactory;
import org.apache.servicemix.jdbc.Statements;
import org.apache.servicemix.store.Store;
import org.apache.servicemix.store.StoreFactory;

public class JdbcStoreFactory implements StoreFactory {

    private boolean transactional;
    private boolean clustered;
    private DataSource dataSource;
    private IdGenerator idGenerator = new IdGenerator();
    private Map stores = new HashMap();
    private String tableName = "SM_STORE";
    private boolean createDataBase = true;
    private JDBCAdapter adapter;
    private Statements statements;
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.store.ExchangeStoreFactory#get(java.lang.String)
     */
    public synchronized Store open(String name) throws IOException {
        if (adapter == null) {
            Connection connection = null;
            try {
                connection = getDataSource().getConnection();
                adapter = JDBCAdapterFactory.getAdapter(connection);
                if (statements == null) {
                    statements = new Statements();
                    statements.setStoreTableName(tableName);
                }
                adapter.setStatements(statements);
                if (createDataBase) {
                    adapter.doCreateTables(connection);
                }
                connection.commit();
            } catch (SQLException e) {
                throw (IOException) new IOException("Exception while creating database").initCause(e); 
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        JdbcStore store = (JdbcStore) stores.get(name);
        if (store == null) {
            store = new JdbcStore(this, name);
            stores.put(name, store);
        }
        return store;
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.store.ExchangeStoreFactory#release(org.apache.servicemix.store.ExchangeStore)
     */
    public synchronized void close(Store store) throws IOException {
        stores.remove(store);
    }
    
    /**
     * @return Returns the adapter.
     */
    public JDBCAdapter getAdapter() {
        return adapter;
    }
    
    /**
     * @return Returns the dataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource The dataSource to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return Returns the clustered.
     */
    public boolean isClustered() {
        return clustered;
    }

    /**
     * @param clustered The clustered to set.
     */
    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    /**
     * @return Returns the transactional.
     */
    public boolean isTransactional() {
        return transactional;
    }

    /**
     * @param transactional The transactional to set.
     */
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    /**
     * @return Returns the idGenerator.
     */
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * @param idGenerator The idGenerator to set.
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * @return Returns the tableName.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return Returns the createDataBase.
     */
    public boolean isCreateDataBase() {
        return createDataBase;
    }

    /**
     * @param createDataBase The createDataBase to set.
     */
    public void setCreateDataBase(boolean createDataBase) {
        this.createDataBase = createDataBase;
    }
    
}
