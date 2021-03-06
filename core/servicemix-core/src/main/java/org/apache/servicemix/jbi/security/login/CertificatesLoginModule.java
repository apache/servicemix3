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
package org.apache.servicemix.jbi.security.login;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.servicemix.jbi.security.GroupPrincipal;
import org.apache.servicemix.jbi.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This login module authenticate users given an X509 certificate.
 */
public class CertificatesLoginModule implements LoginModule {

    private static final String USER_FILE = "org.apache.servicemix.security.certificates.user";
    private static final String GROUP_FILE = "org.apache.servicemix.security.certificates.group";

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CertificatesLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean debug;
    private String usersFile;
    private String groupsFile;
    private Properties users = new Properties();
    private Properties groups = new Properties();
    private String user;
    private Set principals = new HashSet();
    private File baseDir;

    public void initialize(Subject sub, CallbackHandler handler, Map sharedState, Map options) {
        this.subject = sub;
        this.callbackHandler = handler;

        if (System.getProperty("java.security.auth.login.config") != null) {
            baseDir = new File(System.getProperty("java.security.auth.login.config")).getParentFile();
        } else {
            baseDir = new File(".");
        }

        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        usersFile = (String) options.get(USER_FILE) + "";
        groupsFile = (String) options.get(GROUP_FILE) + "";

        if (debug) {
            LOGGER.debug("Initialized debug=" + debug + " usersFile=" + usersFile + " groupsFile=" + groupsFile
                            + " basedir=" + baseDir);
        }
    }

    public boolean login() throws LoginException {
        File f = new File(baseDir, usersFile);
        InputStream fis = null;
        try {
            fis = new java.io.FileInputStream(f);
            users.load(fis);
        } catch (IOException ioe) {
            throw new LoginException("Unable to load user properties file " + f);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    fis = null;
                } catch (IOException e) {
                    throw new LoginException("Unable to close user properties file " + f);
                }
            }
        }
        f = new File(baseDir, groupsFile);
        try {
            fis = new java.io.FileInputStream(f);
            groups.load(fis);
        } catch (IOException ioe) {
            throw new LoginException("Unable to load group properties file " + f);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    fis = null;
                } catch (IOException e) {
                    throw new LoginException("Unable to close group properties file " + f);
                }
            }
        }

        Callback[] callbacks = new Callback[1];
        callbacks[0] = new CertificateCallback();
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user");
        }
        X509Certificate cert = ((CertificateCallback) callbacks[0]).getCertificate();
        if (cert == null) {
            throw new FailedLoginException("Unable to retrieve certificate");
        }

        Principal principal = cert.getSubjectX500Principal();
        String certName = principal.getName(); 
        for (Iterator it = users.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            if (certName.equals(entry.getValue())) {
                user = (String) entry.getKey();
                principals.add(principal);
                if (debug) {
                    LOGGER.debug("login {}", user);
                }
                return true;
            }
        }
        throw new FailedLoginException();
    }

    public boolean commit() throws LoginException {
        principals.add(new UserPrincipal(user));

        for (Enumeration enumeration = groups.keys(); enumeration.hasMoreElements();) {
            String name = (String) enumeration.nextElement();
            String[] userList = ((String) groups.getProperty(name) + "").split(",");
            for (int i = 0; i < userList.length; i++) {
                if (user.equals(userList[i])) {
                    principals.add(new GroupPrincipal(name));
                    break;
                }
            }
        }

        subject.getPrincipals().addAll(principals);

        clear();

        if (debug) {
            LOGGER.debug("commit");
        }
        return true;
    }

    public boolean abort() throws LoginException {
        clear();

        if (debug) {
            LOGGER.debug("abort");
        }
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().removeAll(principals);
        principals.clear();

        if (debug) {
            LOGGER.debug("logout");
        }
        return true;
    }

    private void clear() {
        groups.clear();
        user = null;
    }

}
