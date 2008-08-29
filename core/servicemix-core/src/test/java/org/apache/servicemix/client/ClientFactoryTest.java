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
package org.apache.servicemix.client;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.api.Destination;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * @author <a href="mailto:gnodet [at] apache.org">Guillaume Nodet</a>
 */
public class ClientFactoryTest extends TestCase {

    private JBIContainer jbi;
    
    protected void setUp() throws Exception {
        jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.init();
        jbi.start();
    }
    
    protected void shutDown() throws Exception {
        jbi.shutDown();
    }
    
    public void testClientFactory() throws Exception {
        ActivationSpec as = new ActivationSpec();
        as.setId("echo");
        as.setComponent(new EchoComponent());
        as.setService(new QName("echo"));
        jbi.activateComponent(as);
        
        JndiObjectFactoryBean fb = new JndiObjectFactoryBean();
        fb.setJndiName(ClientFactory.DEFAULT_JNDI_NAME);
        fb.afterPropertiesSet();
        ClientFactory cf = (ClientFactory) fb.getObject();
        ServiceMixClient client = cf.createClient();
        
        Destination dest = client.createDestination("service::echo");
        InOut me = dest.createInOutExchange();
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        client.sendSync(me);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        client.done(me);
        client.close();
    }
}
