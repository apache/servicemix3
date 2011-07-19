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
package org.apache.servicemix.components.http;

import java.io.IOException;
import java.net.URL;
import java.security.Security;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.thread.BoundedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * An embedded Servlet engine to implement a HTTP connector
 *
 * @version $Revision$
 */
public class HttpsConnector extends HttpInOutBinding {

	private static final Logger logger = LoggerFactory.getLogger(HttpsConnector.class);

    private SslSocketConnector listener = new SslSocketConnector();
    
	/**
	 * The maximum number of threads for the Jetty SocketListener. It's set 
	 * to 256 by default to match the default value in Jetty. 
	 */
	private int maxThreads = 256;
    private Server server;
    private String host;
    private int port;
    private String keyPassword;
    private String keyStore;
    private String keyStorePassword;
    private String keyStoreType = "JKS"; // type of the key store
    private String protocol = "TLS";
    // cert algorithm
    private String keyManagerFactoryAlgorithm = Security.getProperty(
        "ssl.KeyManagerFactory.algorithm") == null
        ? "SunX509" : Security.getProperty("ssl.KeyManagerFactory.algorithm");
    // cert algorithm
    private String trustManagerFactoryAlgorithm = Security.getProperty(
        "ssl.TrustManagerFactory.algorithm") == null
        ? "SunX509" : Security.getProperty("ssl.TrustManagerFactory.algorithm");
    private boolean wantClientAuth = false;
    private boolean needClientAuth = false;

    /**
     * Constructor
     *
     * @param host
     * @param port
     */
    public HttpsConnector(String host, int port, String keyPassword, String keyStorePassword, String keyStore, boolean needClientAuth, boolean wantClientAuth) {
        this.host = host;
        this.port = port;
        this.keyPassword = keyPassword;
        this.keyStorePassword = keyStorePassword;
        this.keyStore = keyStore;
        this.wantClientAuth = wantClientAuth;
        this.needClientAuth = needClientAuth;
    }

    public HttpsConnector() {
    }

    /**
     * Constructor
     *
     * @param listener
     */
    public HttpsConnector(SslSocketConnector listener) {
        this.listener = listener;
    }

    /**
     * Called when the Component is initialized
     *
     * @param cc
     * @throws JBIException
     */
    public void init(ComponentContext cc) throws JBIException {
        super.init(cc);
        //should set all ports etc here - from the naming context I guess ?
        if (keyStore == null) {
            keyStore = System.getProperty("javax.net.ssl.keyStore", "");
            if (keyStore == null) {
                throw new IllegalArgumentException("keyStore or system property javax.net.ssl.keyStore must be set");
            }
        }
        if (keyStore.startsWith("classpath:")) {
            try {
                String res = keyStore.substring(10);
                URL url = new ClassPathResource(res).getURL();
                keyStore = url.toString();
            } catch (IOException e) {
                throw new JBIException("Unable to find keystore " + keyStore, e);
            }
        }
        if (keyStorePassword == null) {
            keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
            if (keyStorePassword == null) {
                throw new IllegalArgumentException("keyStorePassword or system property javax.net.ssl.keyStorePassword must be set");
            }
        }
        if (listener == null) {
            listener = new SslSocketConnector();
        }
        listener.setHost(host);
        listener.setPort(port);
        listener.setConfidentialPort(port);
        listener.setPassword(keyStorePassword);
        listener.setKeyPassword(keyPassword != null ? keyPassword : keyStorePassword);
        listener.setKeystore(keyStore);
        listener.setWantClientAuth(wantClientAuth);
        listener.setNeedClientAuth(needClientAuth);
        listener.setProtocol(protocol);
        listener.setSslKeyManagerFactoryAlgorithm(keyManagerFactoryAlgorithm);
        listener.setSslTrustManagerFactoryAlgorithm(trustManagerFactoryAlgorithm);
        listener.setKeystoreType(keyStoreType);
        server = new Server();
        BoundedThreadPool btp = new BoundedThreadPool();
        btp.setMaxThreads(getMaxThreads());
        server.setThreadPool(btp);
    }
    
    /**
     * start the Component
     *
     * @throws JBIException
     */
    public void start() throws JBIException {
        server.setConnectors(new Connector[] { listener });
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        ServletHolder holder = new ServletHolder();
        holder.setName("jbiServlet");
        holder.setClassName(BindingServlet.class.getName());
        ServletHandler handler = new ServletHandler();
        handler.setServlets(new ServletHolder[] { holder });
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName("jbiServlet");
        mapping.setPathSpec("/*");
        handler.setServletMappings(new ServletMapping[] { mapping });
        context.setHandler(handler);
        server.setHandler(context);
        context.setAttribute("binding", this);
        try {
            server.start();
        }
        catch (Exception e) {
        	logger.warn(e.toString());
            throw new JBIException("Start failed: " + e, e);
        }
    }

    /**
     * stop
     */
    public void stop() throws JBIException {
        try {
            if (server != null) {
                server.stop();
            }
        }
        catch (Exception e) {
        	logger.warn(e.toString());
            throw new JBIException("Stop failed: " + e, e);
        }
    }

    /**
     * shutdown
     */
    public void shutDown() throws JBIException {
        server = null;
    }

    // Properties
    //-------------------------------------------------------------------------
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

    /**
     * @return Returns the algorithm.
     */
    public String getKeyManagerFactoryAlgorithm() {
        return keyManagerFactoryAlgorithm;
    }

    /**
     * @param algorithm The algorithm to set.
     */
    public void setKeyManagerFactoryAlgorithm(String algorithm) {
        this.keyManagerFactoryAlgorithm = algorithm;
    }

    /**
     * @return Returns the algorithm.
     */
    public String getTrustManagerFactoryAlgorithm() {
        return trustManagerFactoryAlgorithm;
    }

    /**
     * @param algorithm The algorithm to set.
     */
    public void setTrustManagerFactoryAlgorithm(String algorithm) {
        this.trustManagerFactoryAlgorithm = algorithm;
    }

    /**
     * @return Returns the keyPassword.
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * @param keyPassword The keyPassword to set.
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * @return Returns the keyStore.
     */
    public String getKeyStore() {
        return keyStore;
    }

    /**
     * @param keyStore The keyStore to set.
     */
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * @return Returns the keyStorePassword.
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * @param keyStorePassword The keyStorePassword to set.
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * @return Returns the keyStoreType.
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * @param keyStoreType The keyStoreType to set.
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * @return Returns the needClientAuth.
     */
    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    /**
     * @param needClientAuth The needClientAuth to set.
     */
    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    /**
     * @return Returns the protocol.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol The protocol to set.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return Returns the wantClientAuth.
     */
    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    /**
     * @param wantClientAuth The wantClientAuth to set.
     */
    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

}
