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
package org.apache.servicemix.jbi.nmr.flow.jms;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.tck.ReceiverComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class MultipleJMSFlowTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(MultipleJMSFlowTest.class);

    protected BrokerService broker;
    
    protected void setUp() throws Exception {
        BrokerFactoryBean bfb = new BrokerFactoryBean(new ClassPathResource("org/apache/servicemix/jbi/nmr/flow/jca/broker.xml"));
        bfb.afterPropertiesSet();
        broker = bfb.getBroker();
        broker.start();
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        broker.stop();
    }
    
    public void test() throws Exception {
        JBIContainer[] containers = new JBIContainer[4];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new JBIContainer();
            containers[i].setName("container" + i);
            containers[i].setFlow(createFlow());
            containers[i].setEmbedded(true);
            containers[i].setMonitorInstallationDirectory(false);
            containers[i].setUseMBeanServer(false);
            containers[i].setCreateMBeanServer(false);
            containers[i].init();
        }
        long t0 = System.currentTimeMillis();
        printNodes(containers);
        for (int i = 0; i < containers.length; i++) {
            containers[i].start();
            printNodes(containers);
        }
        long t1 = System.currentTimeMillis();
        LOGGER.info(Long.toString(t1 - t0));
        for (int i = 0; i < containers.length; i++) {
            containers[i].activateComponent(new ReceiverComponent(), "receiver");
        }
        for (int i = 0; i < containers.length; i++) {
            containers[i].stop();
            printNodes(containers);
        }
        for (int i = 0; i < containers.length; i++) {
            containers[i].start();
            printNodes(containers);
        }
        for (int i = 0; i < containers.length; i++) {
            containers[i].stop();
            printNodes(containers);
        }
        for (int i = 0; i < containers.length; i++) {
            containers[i].shutDown();
            printNodes(containers);
        }
    }
    
    protected void printNodes(JBIContainer[] containers) {
        String msg = "Nodes: ";
        for (int j = 0; j < containers.length; j++) {
            if (j > 0) {
                msg += ", ";
            }
            msg += ((JMSFlow) containers[j].getFlow()).numberInNetwork();
        }
        LOGGER.info(msg);
    }
    
    protected Flow createFlow() {
        JMSFlow flow = new JMSFlow();
        flow.setJmsURL("tcp://localhost:61216");
        return flow;
    }
    
}
