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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;

import org.apache.activeio.util.ByteArrayInputStream;
import org.apache.activeio.util.ByteArrayOutputStream;
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
            factory.getAdapter().doStoreData(connection, name + ":" + id, buffer.toByteArray());
            connection.commit();
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
            byte[] data = factory.getAdapter().doLoadData(connection, name + ":" + id);
            Object result = null;
            if (data != null) {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                result = ois.readObject();
                factory.getAdapter().doRemoveData(connection, name + ":" + id);
            }
            connection.commit();
            return result;
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
