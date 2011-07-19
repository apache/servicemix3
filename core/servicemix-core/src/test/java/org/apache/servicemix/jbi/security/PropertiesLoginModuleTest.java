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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Security;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoginModuleTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(PropertiesLoginModuleTest.class);
    
    static {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            URL resource = PropertiesLoginModuleTest.class.getResource("login.properties");
            if (resource != null) {
                path = new File(resource.getFile()).getAbsolutePath();
                System.setProperty("java.security.auth.login.config", path);
            }
        }
        LOGGER.info("Path to login config: {}", path);
        //
        // This test depends on the "policy.allowSystemProperty" security
        // property being set to true.  If we don't ensure it is set here,
        // ibmjdk 5 SR2 will fail with the following message:
        // "Unable to locate a login configuration".
        //
        try {
            if (!"true".equals(Security.getProperty("policy.allowSystemProperty"))) {
                Security.setProperty("policy.allowSystemProperty", "true");
                LOGGER.info("Reset security property 'policy.allowSystemProperty' to 'true'");
            }
        } catch (SecurityException e) {
             // Ignore.
        }
    }

    public void testLogin() throws LoginException {
        LoginContext context = new LoginContext("servicemix-domain", new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        ((NameCallback) callbacks[i]).setName("first");
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        ((PasswordCallback) callbacks[i]).setPassword("secret".toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        });
        context.login();

        Subject subject = context.getSubject();

        assertEquals("Should have three principals", 3, subject.getPrincipals().size());
        assertEquals("Should have one user principal", 1, subject.getPrincipals(UserPrincipal.class).size());
        assertEquals("Should have two group principals", 2, subject.getPrincipals(GroupPrincipal.class).size());

        context.logout();

        assertEquals("Should have zero principals", 0, subject.getPrincipals().size());
    }

    public void testBadUseridLogin() throws Exception {
        LoginContext context = new LoginContext("servicemix-domain", new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        ((NameCallback) callbacks[i]).setName("BAD");
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        ((PasswordCallback) callbacks[i]).setPassword("secret".toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        });
        try {
            context.login();
            fail("Should have thrown a FailedLoginException");
        } catch (FailedLoginException doNothing) {
            // Expected
        }
    }

    public void testBadPWLogin() throws Exception {
        LoginContext context = new LoginContext("servicemix-domain", new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        ((NameCallback) callbacks[i]).setName("first");
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        ((PasswordCallback) callbacks[i]).setPassword("BAD".toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        });
        try {
            context.login();
            fail("Should have thrown a FailedLoginException");
        } catch (FailedLoginException doNothing) {
            // Expected
        }
    }

}
