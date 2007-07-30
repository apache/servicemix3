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
package org.apache.servicemix.jmx;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * 
 * @author gnodet
 */
public class JaasAuthenticator implements JMXAuthenticator {

    private String domain = "servicemix-domain";

    /**
     * @return the JAAS domain to use for authentication
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain the JAAS domain to use for authentication
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /* (non-Javadoc)
     * @see javax.management.remote.JMXAuthenticator#authenticate(java.lang.Object)
     */
    public Subject authenticate(Object credentials) throws SecurityException {
        if (!(credentials instanceof String[])) {
            throw new IllegalArgumentException("Expected String[2], got " + (credentials != null ? credentials.getClass().getName() : null));
        }
        String[] params = (String[]) credentials;
        if (params.length != 2) {
            throw new IllegalArgumentException("Expected String[2] but length was " + params.length);
        }
        Subject subject = new Subject();
        try {
            authenticate(subject, domain, params[0], params[1]);
        } catch (LoginException e) {
            throw new SecurityException("Authentication failed", e);
        } catch (Exception e) {
            throw new SecurityException("Error occured while authenticating", e);
        }
        return subject;
    }

    protected void authenticate(Subject subject,
                                String domain,
                                final String user, 
                                final Object credentials) throws GeneralSecurityException {
        LoginContext loginContext = new LoginContext(domain, subject, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(user);
                    } else if (callback instanceof PasswordCallback && credentials instanceof String) {
                        ((PasswordCallback) callback).setPassword(((String) credentials).toCharArray());
                    } else if (callback instanceof CertificateCallback && credentials instanceof X509Certificate) {
                        ((CertificateCallback) callback).setCertificate((X509Certificate) credentials);
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }
        });
        loginContext.login();
        loginContext.logout();
    }

}
