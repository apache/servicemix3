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
package org.apache.servicemix.jbi.messaging;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;
import org.springframework.core.io.ClassPathResource;

/**
 * @version $Revision$
 */
public class JcaFlowPersistentTest extends AbstractPersistenceTest {

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

    protected Flow createFlow() {
        return new JCAFlow("tcp://localhost:61216");
    }

    public void testSyncSendSyncReceive() throws Exception {
        try {
            runSimpleTest(true, true);
            fail("sendSync can not be used");
        } catch (Exception e) {
            // sendSync can not be used
        }
    }

    public void testAsyncSendSyncReceive() throws Exception {
        runSimpleTest(false, true);
    }

    public void testSyncSendAsyncReceive() throws Exception {
        try {
            runSimpleTest(true, false);
            fail("sendSync can not be used");
        } catch (Exception e) {
            // sendSync can not be used
        }
    }

    public void testAsyncSendAsyncReceive() throws Exception {
        runSimpleTest(false, false);
    }

}
