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

import javax.jbi.messaging.InOnly;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class SpringSecuredRemoteBrokerTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringSecuredRemoteBrokerTest.class);

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

    protected ServiceMixClient client;
    protected SpringJBIContainer jbi1;
    protected SpringJBIContainer jbi2;
    protected AbstractXmlApplicationContext context;
    protected BrokerService broker;

    protected void setUp() throws Exception {
        BrokerFactoryBean bfb = new BrokerFactoryBean(new ClassPathResource("org/apache/servicemix/jbi/nmr/flow/jca/broker.xml"));
        bfb.afterPropertiesSet();
        broker = bfb.getBroker();
        broker.start();

        context = createBeanFactory();
        jbi1 = (SpringJBIContainer) context.getBean("jbi1");
        jbi2 = (SpringJBIContainer) context.getBean("jbi2");

        assertNotNull("JBI Container not found in spring!", jbi1);
        assertNotNull("JBI Container not found in spring!", jbi2);

        client = new DefaultServiceMixClient(jbi1);
    }

    protected void tearDown() throws Exception {
        if (context != null) {
            LOGGER.info("Closing down the spring context");
            context.destroy();
        }
        broker.stop();
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/jbi/security/secure2.xml");
    }

    protected Subject login(final String username, final String password) throws Exception {
        LoginContext logincontext = new LoginContext("servicemix-domain", new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        ((NameCallback) callbacks[i]).setName(username);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        });
        logincontext.login();
        return logincontext.getSubject();
    }

    protected void send(String username, String password, QName service) throws Exception {
        Subject subject = login(username, password);
        InOnly me = client.createInOnlyExchange();
        me.setService(service);
        me.getInMessage().setSecuritySubject(subject);
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        client.sendSync(me);
    }

    public void testAuthorizationsOnLocalReceiver1() throws Exception {
        QName service = new QName("http://servicemix.org/example/1", "receiver1");
        // receiver1 should be available to: programmers, accounting, testers
        send("first", "secret", service);
        send("second", "password", service);
        send("third", "another", service);
    }

    public void testAuthorizationsOnLocalReceiver2() throws Exception {
        QName service = new QName("http://servicemix.org/example/1", "receiver2");
        // receiver2 should be available to: programmers, accounting
        send("first", "secret", service);
        send("second", "password", service);
        try {
            send("third", "another", service);
            fail("receiver2 is not available to testers");
        } catch (SecurityException e) {
            // Expected
        }
    }

    public void testAuthorizationsOnLocalReceiver3() throws Exception {
        QName service = new QName("http://servicemix.org/example/1a", "receiver1");
        // receiver3 should be available to: programmers
        send("first", "secret", service);
        try {
            send("second", "password", service);
            fail("receiver2 is not available to accounting");
        } catch (SecurityException e) {
            // Expected
        }
        try {
            send("third", "another", service);
            fail("receiver2 is not available to testers");
        } catch (SecurityException e) {
            // Expected
        }
    }

    public void testAuthorizationsOnRemoteReceiver() throws Exception {
        QName service = new QName("http://servicemix.org/example/2a", "receiver1");
        // remote endpoint should be available to: programmers
        send("first", "secret", service);
        try {
            send("second", "password", service);
            fail("receiver3 is not available to accounting");
        } catch (SecurityException e) {
            // Expected
        }
        
        try {
            send("third", "another", service);
            fail("receiver3 is not available to testers");
        } catch (SecurityException e) {
            // Expected
        }
    }

}
