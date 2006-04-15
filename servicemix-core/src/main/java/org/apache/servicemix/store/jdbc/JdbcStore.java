package org.apache.servicemix.store.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.store.Store;

public class JdbcStore implements Store {

    private static final Log log = LogFactory.getLog(JdbcStore.class);

    private JdbcStoreFactory factory;
    private String name;
    
    public JdbcStore(JdbcStoreFactory factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    public boolean hasFeature(String name) {
        return PERSISTENT.equals(name) ||
               (CLUSTERED.equals(name) && factory.isClustered()) ||
               (TRANSACTIONAL.equals(name) && factory.isTransactional());
    }

    public void store(String id, Object data) throws IOException {
        log.debug("Storing object with id: " + id);
        Connection connection = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            out.writeObject(data);
            out.close();
            connection = factory.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO " + factory.getTableName() + " (ID, DATA) VALUES (?, ?)");
            ps.setString(1, name + ":" + id);
            ps.setBytes(2, buffer.toByteArray());
            ps.execute();
        } catch (Exception e) {
            throw (IOException) new IOException("Error storing object").initCause(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    throw (IOException) new IOException("Error closing connection").initCause(e);
                }
            }
        }
    }

    public String store(Object data) throws IOException {
        String id = factory.getIdGenerator().generateId();
        store(id, data);
        return id;
    }

    public Object load(String id) throws IOException {
        log.debug("Loading object with id: " + id);
        Connection connection = null;
        try {
            connection = factory.getDataSource().getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT DATA FROM " + factory.getTableName() + " WHERE ID = ?");
            ps.setString(1, name + ":" + id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] data = rs.getBytes(1);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                Object result = ois.readObject();
                ps = connection.prepareStatement("DELETE FROM " + factory.getTableName() + " WHERE ID = ?");
                ps.setString(1, name + ":" + id);
                ps.execute();
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw (IOException) new IOException("Error storing object").initCause(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    throw (IOException) new IOException("Error closing connection").initCause(e);
                }
            }
        }
    }

}
