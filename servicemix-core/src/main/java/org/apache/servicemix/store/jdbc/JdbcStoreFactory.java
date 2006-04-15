package org.apache.servicemix.store.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.activemq.util.IdGenerator;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
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
    private boolean firstStore = true;
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.store.ExchangeStoreFactory#get(java.lang.String)
     */
    public synchronized Store open(String name) throws IOException {
        if (firstStore && createDataBase) {
            firstStore = false;
            try {
                createDataBase();
            } catch (Exception e) {
                throw (IOException) new IOException("Exception while creating database").initCause(e); 
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
    
    public void createDataBase() throws SQLException {
        Database db = new Database();
        db.setName("JdbcStore");
        Table table = new Table();
        table.setName(tableName);
        Column id = new Column();
        id.setName("ID");
        id.setType("VARCHAR");
        id.setPrimaryKey(true);
        id.setRequired(true);
        table.addColumn(id);
        Column exchange = new Column();
        exchange.setName("DATA");
        exchange.setType("BLOB");
        table.addColumn(exchange);
        db.addTable(table);

        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);
        platform.createTables(db, false, true);
    }

}
