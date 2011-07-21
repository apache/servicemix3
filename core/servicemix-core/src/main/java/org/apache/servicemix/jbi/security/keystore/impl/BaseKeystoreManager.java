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
package org.apache.servicemix.jbi.security.keystore.impl;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.servicemix.jbi.security.keystore.KeystoreInstance;
import org.apache.servicemix.jbi.security.keystore.KeystoreIsLocked;
import org.apache.servicemix.jbi.security.keystore.KeystoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @org.apache.xbean.XBean element="keystoreManager"
 */
public class BaseKeystoreManager implements KeystoreManager {

    protected static final transient Logger LOGGER = LoggerFactory.getLogger(BaseKeystoreManager.class);
    
    protected KeystoreInstance[] keystores;

    /**
     * @return the keystores
     */
    public KeystoreInstance[] getKeystores() {
        return keystores;
    }

    /**
     * @param keystores the keystores to set
     */
    public void setKeystores(KeystoreInstance[] keystores) {
        this.keystores = keystores;
    }

    /**
     * Gets a SocketFactory using one Keystore to access the private key and
     * another to provide the list of trusted certificate authorities.
     * 
     * @param provider
     *            The SSL provider to use, or null for the default
     * @param protocol
     *            The SSL protocol to use
     * @param algorithm
     *            The SSL algorithm to use
     * @param keyStore
     *            The key keystore name as provided by listKeystores. The
     *            KeystoreInstance for this keystore must be unlocked.
     * @param keyAlias
     *            The name of the private key in the keystore. The
     *            KeystoreInstance for this keystore must have unlocked this
     *            key.
     * @param trustStore
     *            The trust keystore name as provided by listKeystores. The
     *            KeystoreInstance for this keystore must have unlocked this
     *            key.
     * 
     * @return A created SSLSocketFactory item created from the KeystoreManager.
     * @throws GeneralSecurityException 
     * @throws KeystoreIsLocked
     *             Occurs when the requested key keystore cannot be used because
     *             it has not been unlocked.
     */
    public SSLSocketFactory createSSLFactory(String provider, String protocol, 
                                             String algorithm, String keyStore,
                                             String keyAlias, String trustStore) throws GeneralSecurityException  {
        // the keyStore is optional.
        KeystoreInstance keyInstance = null;
        if (keyStore != null) {
            keyInstance = getKeystore(keyStore);
            if (keyInstance.isKeystoreLocked()) {
                throw new KeystoreIsLocked("Keystore '" + keyStore
                                + "' is locked; please use the keystore page in the admin console to unlock it");
            }
            if (keyInstance.isKeyLocked(keyAlias)) {
                throw new KeystoreIsLocked("Key '" + keyAlias + "' in keystore '" + keyStore
                                + "' is locked; please use the keystore page in the admin console to unlock it");
            }
        }
        KeystoreInstance trustInstance = trustStore == null ? null : getKeystore(trustStore);
        if (trustInstance != null && trustInstance.isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '" + trustStore
                            + "' is locked; please use the keystore page in the admin console to unlock it");
        }

        // OMG this hurts, but it causes ClassCastExceptions elsewhere unless
        // done this way!
        try {
            /*
            Class cls = loader.loadClass("javax.net.ssl.SSLContext");
            Object ctx = cls.getMethod("getInstance", new Class[] { String.class }).invoke(null,
                            new Object[] { protocol });
            Class kmc = loader.loadClass("[Ljavax.net.ssl.KeyManager;");
            Class tmc = loader.loadClass("[Ljavax.net.ssl.TrustManager;");
            Class src = loader.loadClass("java.security.SecureRandom");
            cls.getMethod("init", new Class[] { kmc, tmc, src }).invoke(
                            ctx,
                            new Object[] { keyInstance == null ? null : keyInstance.getKeyManager(algorithm, keyAlias),
                                            trustInstance == null ? null : trustInstance.getTrustManager(algorithm),
                                            new java.security.SecureRandom() });
            Object result = cls.getMethod("getSocketFactory", new Class[0]).invoke(ctx, new Object[0]);
            return (SSLSocketFactory) result;
            */
            SSLContext context;
            if (provider == null) {
                context = SSLContext.getInstance(protocol);
            } else {
                context = SSLContext.getInstance(protocol, provider);
            }
            context.init(keyInstance == null ? null : keyInstance.getKeyManager(algorithm, keyAlias), 
                         trustInstance == null ? null : trustInstance.getTrustManager(algorithm), 
                                         new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception e) {
            LOGGER.error("Unable to dynamically load", e);
            return null;
        }
    }

    /**
     * Gets a ServerSocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     * 
     * @param provider
     *            The SSL provider to use, or null for the default
     * @param protocol
     *            The SSL protocol to use
     * @param algorithm
     *            The SSL algorithm to use
     * @param keyStore
     *            The key keystore name as provided by listKeystores. The
     *            KeystoreInstance for this keystore must be unlocked.
     * @param keyAlias
     *            The name of the private key in the keystore. The
     *            KeystoreInstance for this keystore must have unlocked this
     *            key.
     * @param trustStore
     *            The trust keystore name as provided by listKeystores. The
     *            KeystoreInstance for this keystore must have unlocked this
     *            key.
     * 
     * @throws KeystoreIsLocked
     *             Occurs when the requested key keystore cannot be used because
     *             it has not been unlocked.
     */
    public SSLServerSocketFactory createSSLServerFactory(String provider, String protocol, 
                                                         String algorithm, String keyStore, 
                                                         String keyAlias, String trustStore) throws GeneralSecurityException {
        KeystoreInstance keyInstance = getKeystore(keyStore);
        if (keyInstance.isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '" + keyStore
                            + "' is locked; please use the keystore page in the admin console to unlock it");
        }
        if (keyInstance.isKeyLocked(keyAlias)) {
            throw new KeystoreIsLocked("Key '" + keyAlias + "' in keystore '" + keyStore
                            + "' is locked; please use the keystore page in the admin console to unlock it");
        }
        KeystoreInstance trustInstance = trustStore == null ? null : getKeystore(trustStore);
        if (trustInstance != null && trustInstance.isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '" + trustStore
                            + "' is locked; please use the keystore page in the admin console to unlock it");
        }

        // OMG this hurts, but it causes ClassCastExceptions elsewhere unless
        // done this way!
        try {
            /*
            Class cls = loader.loadClass("javax.net.ssl.SSLContext");
            Object ctx = cls.getMethod("getInstance", new Class[] { String.class }).invoke(null,
                            new Object[] { protocol });
            Class kmc = loader.loadClass("[Ljavax.net.ssl.KeyManager;");
            Class tmc = loader.loadClass("[Ljavax.net.ssl.TrustManager;");
            Class src = loader.loadClass("java.security.SecureRandom");
            cls.getMethod("init", new Class[] { kmc, tmc, src }).invoke(
                            ctx,
                            new Object[] { keyInstance.getKeyManager(algorithm, keyAlias),
                                            trustInstance == null ? null : trustInstance.getTrustManager(algorithm),
                                            new java.security.SecureRandom() });
            Object result = cls.getMethod("getServerSocketFactory", new Class[0]).invoke(ctx, new Object[0]);
            return (SSLServerSocketFactory) result;
            */
            SSLContext context;
            if (provider == null) {
                context = SSLContext.getInstance(protocol);
            } else {
                context = SSLContext.getInstance(protocol, provider);
            }
            context.init(keyInstance == null ? null : keyInstance.getKeyManager(algorithm, keyAlias), 
                         trustInstance == null ? null : trustInstance.getTrustManager(algorithm), 
                                         new SecureRandom());
            return context.getServerSocketFactory();
        } catch (Exception e) {
            LOGGER.error("Unable to dynamically load", e);
            return null;
        }
    }

    public KeystoreInstance getKeystore(String name) {
        if (keystores != null) {
            for (int i = 0; i < keystores.length; i++) {
                if (name.equals(keystores[i].getName())) {
                    return keystores[i];
                }
            }
        }
        return null;
    }

}
