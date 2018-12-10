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
package org.apache.servicemix.components.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.TransformComponentSupport;
import org.mortbay.resource.Resource;
import org.springframework.core.io.ClassPathResource;

/**
 * Performs HTTP client invocations on a remote HTTP site.
 *
 * @version $Revision: 373823 $
 */
public class HttpsInvoker extends TransformComponentSupport implements MessageExchangeListener {

    protected HttpClientMarshaler marshaler = new HttpClientMarshaler();
    protected MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    protected HttpClient httpClient = new HttpClient(connectionManager);
    protected HostConfiguration hostConfiguration = new HostConfiguration();
    protected String url;
    protected boolean defaultInOut = true;

    private String keyPassword;
    private String keyStore;
    private String keyStorePassword;
    private String keyStoreType = "JKS"; // type of the key store
    private String trustStore;
    private String trustStorePassword;
    private String trustStoreType = "JKS";
    private String protocol = "TLS";
    private String algorithm = "SunX509"; // cert algorithm
    
    private class CommonsHttpSSLSocketFactory implements SecureProtocolSocketFactory {

        private SSLSocketFactory factory;
        
        public CommonsHttpSSLSocketFactory() throws Exception {
            SSLContext context = SSLContext.getInstance(protocol);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
            if (keyStore == null) {
                keyStore = System.getProperty("javax.net.ssl.keyStore");
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
                    throw new JBIException("Unable to find keyStore " + keyStore, e);
                }
            }
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
                if (keyStorePassword == null) {
                    throw new IllegalArgumentException("keyStorePassword or system property javax.net.ssl.keyStorePassword must be set");
                }
            }
            if (trustStore == null) {
                trustStore = System.getProperty("javax.net.ssl.trustStore");
            }
            if (trustStore != null && trustStore.startsWith("classpath:")) {
                try {
                    String res = trustStore.substring(10);
                    URL url = new ClassPathResource(res).getURL();
                    trustStore = url.toString();
                } catch (IOException e) {
                    throw new JBIException("Unable to find trustStore " + trustStore, e);
                }
            }
            if (trustStorePassword == null) {
                trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
                if (keyStorePassword == null) {
                    throw new IllegalArgumentException("trustStorePassword or system property javax.net.ssl.trustStorePassword must be set");
                }
            }
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            ks.load(Resource.newResource(keyStore).getInputStream(), keyStorePassword.toCharArray());
            keyManagerFactory.init(ks, keyPassword != null ? keyPassword.toCharArray() : keyStorePassword.toCharArray());
            if (trustStore != null) {
                KeyStore ts = KeyStore.getInstance(trustStoreType);
                ts.load(Resource.newResource(trustStore).getInputStream(), trustStorePassword.toCharArray());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
                trustManagerFactory.init(ts);
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());
            } else {
                context.init(keyManagerFactory.getKeyManagers(), null, new java.security.SecureRandom());
            }
            factory = context.getSocketFactory();
        }
        
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return factory.createSocket(socket, host, port, autoClose);
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
            return factory.createSocket(host, port, localAddress, localPort);
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
            if (params == null) {
                throw new IllegalArgumentException("Parameters may not be null");
            }
            int timeout = params.getConnectionTimeout();
            if (timeout == 0) {
                return createSocket(host, port, localAddress, localPort);
            } else {
                Socket socket = factory.createSocket();
                SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
                SocketAddress remoteaddr = new InetSocketAddress(host, port);
                socket.bind(localaddr);
                socket.connect(remoteaddr, timeout);
                return socket;
            }
        }

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return factory.createSocket(host, port);
        }
        
    }
    
    protected void init() throws JBIException {
        super.init();
        try {
            URI uri = new URI(url);
            ProtocolSocketFactory sf = new CommonsHttpSSLSocketFactory();
            Protocol protocol = new Protocol("https", sf, 443);
            HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), protocol);
            hostConfiguration.setHost(host);
        } catch (Exception e) {
            throw new JBIException("Unable to initialize HttpsInvoker", e);
        }
    }
    
    public void stop() throws JBIException {
        super.stop();
        connectionManager.shutdown();
    }

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        String url;
        // We need to relativize the method url to the host config
        // so that the hostConfiguration is not overriden by the executeMethod call
        try {
            java.net.URI uri = new URI(this.url);
            uri = uri.relativize(new URI(hostConfiguration.getHostURL()));
            url = uri.toString();
        } catch (Exception e1) {
            url = this.url;
        }
        PostMethod method = new PostMethod(url);
        try {
            marshaler.fromNMS(method, exchange, in);
            if (method.getRequestHeader("Content-Type") == null) {
                method.setRequestHeader("Content-Type", "text/html; charset=UTF-8");
            }
            int response = httpClient.executeMethod(hostConfiguration, method);

            if (response != HttpStatus.SC_OK) {
                throw new InvalidStatusResponseException(response);
            }

            // now lets grab the output and set it on the out message
            if (defaultInOut) {
                marshaler.toNMS(out, method);
            }
            return defaultInOut;
        }
        catch (Exception e) {
            throw new MessagingException("Error executing http request", e);
        }
        finally {
            method.releaseConnection();
        }
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDefaultInOut() {
        return defaultInOut;
    }

    public void setDefaultInOut(boolean defaultInOut) {
        this.defaultInOut = defaultInOut;
    }

    public HttpClientMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(HttpClientMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return Returns the algorithm.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @param algorithm The algorithm to set.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
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
     * @return Returns the trustStore.
     */
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * @param trustStore The trustStore to set.
     */
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * @return Returns the trustStorePassword.
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * @param trustStorePassword The trustStorePassword to set.
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * @return Returns the trustStoreType.
     */
    public String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * @param trustStoreType The trustStoreType to set.
     */
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

}
