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

import org.apache.servicemix.components.util.OutBinding;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * @version $Revision$
 */
public abstract class JabberComponentSupport extends OutBinding implements InitializingBean, PacketListener {

    protected static final transient Logger logger = LoggerFactory.getLogger(JabberComponentSupport.class);

    private JabberMarshaler marshaler = new JabberMarshaler();
    protected XMPPConnection connection;
    private ConnectionConfiguration connectionConfig;
    private String host;
    private int port;
    protected String user;
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
            this.connectionConfig = new ConnectionConfiguration(this.host, this.port);
            this.connectionConfig.setCompressionEnabled(true);
            this.connectionConfig.setReconnectionAllowed(true);
            this.connectionConfig.setSASLAuthenticationEnabled(true);

            if (this.connection == null) {
                this.connection = new XMPPConnection(this.connectionConfig);
                this.logger.debug("Connecting to server {}", this.host);
                this.connection.connect();

                if (this.login && !this.connection.isAuthenticated()) {
                    if (this.user != null) {
                        this.logger.debug("Logging into Jabber as user: {}", this.user);
                        if (this.password == null) {
                            this.logger.warn("No password configured for user: {}", this.user);
                        }

                        AccountManager accountManager = new AccountManager(this.connection);
                        accountManager.createAccount(this.user, this.password);

                        if (this.resource != null) {
                            this.connection.login(this.user, this.password, this.resource);
                        } else {
                            this.connection.login(this.user, this.password);
                        }
                    } else {
                        this.logger.debug("Logging in anonymously to Jabber on connection: {}", this.connection);
                        this.connection.loginAnonymously();
                    }
                    // now lets send a presence we are available
                    this.connection.sendPacket(new Presence(Presence.Type.available));
                }
            }
        } catch (XMPPException e) {
            throw new JBIException("Failed to login to Jabber. Reason: " + e, e);
        }
    }

    public void stop() throws JBIException {
        if (this.connection != null && this.connection.isConnected()) {
            this.logger.debug("Disconnecting from server {}", this.host);
            this.connection.disconnect();
            this.connection = null;
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
