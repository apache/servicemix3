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
package org.apache.servicemix.jbi.audit.jdbc;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.audit.AuditorMBean;
import org.apache.servicemix.jbi.messaging.ExchangePacket;
import org.apache.servicemix.jbi.messaging.InOnlyImpl;
import org.apache.servicemix.jbi.messaging.InOptionalOutImpl;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeSupport;
import org.apache.servicemix.jbi.messaging.RobustInOnlyImpl;
import org.springframework.beans.factory.InitializingBean;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.sql.DataSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Basic implementation of ServiceMix auditor on a jdbc store.
 * This implementation, for performance purposes, only relies
 * on one table SM_AUDIT with two columns:
 * <ul>
 *   <li><b>ID</b> the exchange id (varchar)</li>
 *   <li><b>EXCHANGE</b> the serialized exchange (blob)</li>
 * </ul>
 * To minimize overhead, the exchange serialized is the undelying
 * {@link org.apache.servicemix.jbi.messaging.ExchangePacket}.
 * 
 * @org.xbean.XBean element="jdbcAuditor" description="The Auditor of message exchanges to a JDBC database"
 * 
 * @author Guillaume Nodet (gnt)
 * @version $Revision$
 * @since 2.1
 */
public class JdbcAuditor extends AbstractAuditor implements InitializingBean {

    public static String DATABASE_MODEL = "database.xml";
    
    private DataSource dataSource;
    private Platform platform;
    private Database database;
    
    public String getDescription() {
        return "JDBC Auditing Service";
    }
    
    public void afterPropertiesSet() throws Exception {
        if (this.container == null) {
            throw new IllegalArgumentException("container should not be null");
        }
        if (this.dataSource == null) {
            throw new IllegalArgumentException("dataSource should not be null");
        }
        platform = PlatformFactory.createNewPlatformInstance(dataSource);
        InputStream is = getClass().getResourceAsStream(DATABASE_MODEL);
        try {
            database = new DatabaseIO().read(new InputStreamReader(is));
        } finally {
            is.close();
        }
        start();
        container.getManagementContext().registerSystemService(this, AuditorMBean.class);
    }

    public void doStart() throws javax.jbi.JBIException {
        platform.createTables(database, false, true);
    }
    
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange instanceof MessageExchangeImpl == false) {
            throw new IllegalArgumentException("exchange should be a MessageExchangeImpl");
        }
        try {
            ExchangePacket packet = ((MessageExchangeImpl) exchange).getPacket();
            String id = packet.getExchangeId();
            byte[] data = packet.getData();
            Connection connection = platform.borrowConnection();
            try {
                store(connection, id, data);
            } finally {
                platform.returnConnection(connection);
            }
        } catch (Exception e) {
            throw new MessagingException("Could not persist exchange", e);
        }
    }
    
    protected void store(Connection connection, String id, byte[] data) throws Exception {
        PreparedStatement selectStatement = null;
        PreparedStatement storeStatement = null;
        try {
            selectStatement = connection.prepareStatement("SELECT ID FROM SM_AUDIT WHERE ID = ?");
            selectStatement.setString(1, id);
            // Update
            if (selectStatement.executeQuery().next()) {
                storeStatement = connection.prepareStatement("UPDATE SM_AUDIT SET EXCHANGE = ? WHERE ID = ?");
                storeStatement.setString(2, id);
                storeStatement.setBytes(1, data);
                storeStatement.execute();
            // Insert
            } else {
                storeStatement = connection.prepareStatement("INSERT INTO SM_AUDIT (ID, EXCHANGE) VALUES (?, ?)");
                storeStatement.setString(1, id);
                storeStatement.setBytes(2, data);
                storeStatement.execute();
            }
        } finally {
            closeStatement(selectStatement);
            closeStatement(storeStatement);
        }
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeCount()
     */
    public int getExchangeCount()throws AuditorException {
        Connection con = platform.borrowConnection();
        Statement statement = null;
        try {
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(ID) FROM SM_AUDIT");
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AuditorException("Could not retrieve exchange count", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges(int, int)
     */
    public MessageExchange[] getExchanges(int fromIndex, int toIndex) throws AuditorException {
        if (fromIndex < 0) {
            throw new IllegalArgumentException("fromIndex should be greater or equal to zero");
        }
        if (toIndex < fromIndex) {
            throw new IllegalArgumentException("toIndex should be greater or equal to fromIndex");
        }
        // Do not hit the database if no exchanges are requested
        if (fromIndex == toIndex) {
            return new MessageExchange[0];
        }
        Connection con = platform.borrowConnection();
        Statement statement = null;
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(toIndex - fromIndex);
            ResultSet rs = statement.executeQuery("SELECT EXCHANGE FROM SM_AUDIT");
            rs.absolute(fromIndex + 1);
            MessageExchange[] exchanges = new MessageExchange[toIndex - fromIndex];
            for (int row = 0; row < toIndex - fromIndex; row++) {
                exchanges[row] = getExchange(rs.getBytes(1));
                if (!rs.next()) {
                    break;
                }
            }
            return exchanges;
        } catch (SQLException e) {
            throw new AuditorException("Could not retrieve exchanges", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeIds(int, int)
     */
    public String[] getExchangeIds(int fromIndex, int toIndex) throws AuditorException {
        if (fromIndex < 0) {
            throw new IllegalArgumentException("fromIndex should be greater or equal to zero");
        }
        if (toIndex < fromIndex) {
            throw new IllegalArgumentException("toIndex should be greater or equal to fromIndex");
        }
        // Do not hit the database if no ids are requested
        if (fromIndex == toIndex) {
            return new String[0];
        }
        Connection con = platform.borrowConnection();
        Statement statement = null;
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(toIndex - fromIndex);
            ResultSet rs = statement.executeQuery("SELECT ID FROM SM_AUDIT");
            rs.absolute(fromIndex + 1);
            String[] ids = new String[toIndex - fromIndex];
            for (int row = 0; row < toIndex - fromIndex; row++) {
                ids[row] = rs.getString(1);
                if (!rs.next()) {
                    break;
                }
            }
            return ids;
        } catch (SQLException e) {
            throw new AuditorException("Could not retrieve exchange ids", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges(java.lang.String[])
     */
    public MessageExchange[] getExchanges(String[] ids) throws AuditorException {
        MessageExchange[] exchanges = new MessageExchange[ids.length];
        Connection con = platform.borrowConnection();
        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement("SELECT EXCHANGE FROM SM_AUDIT WHERE ID = ?");
            for (int i = 0; i < exchanges.length; i++) {
                statement.setString(1, ids[i]);
                ResultSet rs = statement.executeQuery();
                rs.next();
                exchanges[i] = getExchange(rs.getBytes(1));
            }
            return exchanges;
        } catch (SQLException e) {
            throw new AuditorException("Could not retrieve exchanges", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges()
     */
    public int deleteExchanges() throws AuditorException {
        Connection con = platform.borrowConnection();
        Statement statement = null;
        try {
            statement = con.createStatement();
            return statement.executeUpdate("DELETE FROM SM_AUDIT");
        } catch (SQLException e) {
            throw new AuditorException("Could not delete exchanges", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges(int, int)
     */
    public int deleteExchanges(int fromIndex, int toIndex) throws AuditorException {
        if (fromIndex < 0) {
            throw new IllegalArgumentException("fromIndex should be greater or equal to zero");
        }
        if (toIndex < fromIndex) {
            throw new IllegalArgumentException("toIndex should be greater or equal to fromIndex");
        }
        // Do not hit the database if no removal is requested
        if (fromIndex == toIndex) {
            return 0;
        }
        Connection con = platform.borrowConnection();
        Statement statement = null;
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery("SELECT ID FROM SM_AUDIT");
            rs.absolute(fromIndex + 1);
            for (int row = 0; row < toIndex - fromIndex; row++) {
                rs.deleteRow();
                if (!rs.next()) {
                    return row + 1;
                }
            }
            return toIndex - fromIndex;
        } catch (SQLException e) {
            throw new AuditorException("Could not delete exchanges", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges(java.lang.String[])
     */
    public int deleteExchanges(String[] ids) throws AuditorException {
        Connection con = platform.borrowConnection();
        PreparedStatement statement = null;
        try {
            int deleted = 0;
            statement = con.prepareStatement("DELETE FROM SM_AUDIT WHERE ID = ?");
            for (int i = 0; i < ids.length; i++) {
                statement.setString(1, ids[i]);
                deleted += statement.executeUpdate();
            }
            return deleted;
        } catch (SQLException e) {
            throw new AuditorException("Could not delete exchanges", e);
        } finally {
            closeStatement(statement);
            platform.returnConnection(con);
        }
    }
    
    // TODO: this should be somewhere in org.apache.servicemix.jbi.messaging
    protected MessageExchange getExchange(byte[] data) throws AuditorException {
        ExchangePacket packet = null;
        try {
            packet = ExchangePacket.readPacket(data);
        } catch (Exception e) {
            throw new AuditorException("Unable to reconstruct exchange", e);
        }
        URI mep = packet.getPattern();
        if (MessageExchangeSupport.IN_ONLY.equals(mep)) {
            return new InOnlyImpl(packet);
        } else if (MessageExchangeSupport.IN_OPTIONAL_OUT.equals(mep)) {
            return new InOptionalOutImpl(packet);
        } else if (MessageExchangeSupport.IN_OUT.equals(mep)) {
            return new InOutImpl(packet);
        } else if (MessageExchangeSupport.ROBUST_IN_ONLY.equals(mep)) {
            return new RobustInOnlyImpl(packet);
        } else {
            throw new AuditorException("Unhandled mep: " + mep);
        }
    }
    
    /**
     * Close the given statement, logging any exception.
     * @param statement the statement to close
     */
    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                Connection conn = statement.getConnection();
                if ((conn != null) && !conn.isClosed()) {
                    statement.close();
                }
            } catch (Exception e) {
                log.warn("Error closing statement", e);
            }
        }
    }

    
}
