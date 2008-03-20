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
package org.apache.servicemix.jbi.security;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.security.keystore.KeystoreInstance;
import org.apache.servicemix.jbi.security.keystore.KeystoreManager;
import org.apache.servicemix.jbi.security.keystore.impl.BaseKeystoreManager;
import org.apache.servicemix.jbi.security.keystore.impl.FileKeystoreInstance;
import org.springframework.core.io.ClassPathResource;

public class FileKeystoreManagerTest extends TestCase {
    
    private KeystoreManager keystoreManager;
    
    protected void setUp() throws Exception {
        BaseKeystoreManager mgr = new BaseKeystoreManager();
        FileKeystoreInstance keystore = new FileKeystoreInstance();
        keystore.setPath(new ClassPathResource("org/apache/servicemix/jbi/security/privatestore.jks"));
        keystore.setKeystorePassword("keyStorePassword");
        keystore.setKeyPasswords("myalias=myAliasPassword");
        keystore.setName("ks");
        mgr.setKeystores(new KeystoreInstance[] {keystore });
        keystoreManager = mgr;
    }
    
    public void testGetKeystoreInstance() throws Exception {
        assertNotNull(keystoreManager.getKeystore("ks"));
    }

    public void testGetUnknownKeystoreInstance() throws Exception {
        assertNull(keystoreManager.getKeystore("ks2"));
    }
    
    public void testPrivateKey() throws Exception {
        KeystoreInstance ks = keystoreManager.getKeystore("ks");
        String[] pk = ks.listPrivateKeys();
        assertNotNull(pk);
        assertEquals(1, pk.length);
        assertEquals("myalias", pk[0]);
        assertNotNull(ks.getCertificate("myalias"));
        assertNotNull(ks.getPrivateKey("myalias"));
    }

}
