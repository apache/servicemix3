/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.components.jabber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.components.util.OutBinding;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.springframework.beans.factory.InitializingBean;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * @version $Revision: 517251 $
 */
public abstract class JabberComponentSupport extends OutBinding implements InitializingBean, PacketListener {
    private static final transient Log log = LogFactory.getLog(JabberComponentSupport.class);

    private JabberMarshaler marshaler = new JabberMarshaler();
    private XMPPConnection connection;
    private String host;
    private int port;
    private String user;
    private String password;
    private String resource = "ServiceMix";
    private boolean login = true;

    public void afterPropertiesSet() throws Exception {
        if (connection == null) {
            if (host == null) {
                throw new IllegalArgumentException("You must specify the connection or the host property");
            }
        }
    }

    public void start() throws JBIException {
        try {
            if (connection == null) {
                if (port > 0) {
                    connection = new XMPPConnection(host, port);
                }
                else {
                    connection = new XMPPConnection(host);
                }
            }
            if (login && !connection.isAuthenticated()) {
                if (user != null) {
                    AccountManager accountManager = new AccountManager(connection);
                    try {
                        log.info("Logging in to Jabber as user: " + user + " on connection: " + connection);
                        connection.login(user, password, resource);
                    } catch (XMPPException e) {
                        final XMPPError error = e.getXMPPError();
                        // 401 == Not Authorized
                        if (error != null && error.getCode() == 401) {
                            // is ist possible to create Accounts?
                            if (accountManager.supportsAccountCreation()) {
                                //try to create the Account (maybe it wasn't there)
                                accountManager.createAccount(user, password);
                                log.info("Logging in to Jabber as user: " + user + " on connection: " + connection);
                                // try to login again (if this fails we are screwed and fail ultimatively)
                                connection.login(user, password, resource);
                            }
                        }
                    }
                }
                else {
                    log.info("Logging in anonymously to Jabber on connection: " + connection);
                    connection.loginAnonymously();
                }
            }
        }
        catch (XMPPException e) {
            throw new JBIException("Failed to login to Jabber. Reason: " + e, e);
        }
    }

    public void stop() throws JBIException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void processPacket(Packet packet) {
        try {
            InOnly exchange = getExchangeFactory().createInOnlyExchange();
            NormalizedMessage in = exchange.createMessage();
            exchange.setInMessage(in);
            marshaler.toNMS(in, packet);
            done(exchange);
        }
        catch (MessagingException e) {
            throw new JabberListenerException(e, packet);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public JabberMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(JabberMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
}
