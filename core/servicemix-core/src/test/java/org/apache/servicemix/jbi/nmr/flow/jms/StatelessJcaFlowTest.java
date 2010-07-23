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

import javax.transaction.TransactionManager;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;
import org.jencks.GeronimoPlatformTransactionManager;

public class StatelessJcaFlowTest extends StatelessJmsFlowTest {
    
    private TransactionManager tm;

    protected void setUp() throws Exception {
        tm = new GeronimoPlatformTransactionManager();
        super.setUp();
    }

    protected JBIContainer createContainer(String name) throws Exception {
        JBIContainer container = new JBIContainer();
        container.setName(name);
        JCAFlow flow = new JCAFlow(ACTIVEMQ_URL);
        container.setTransactionManager(tm);
        container.setFlow(flow);
        container.setUseMBeanServer(false);
        container.setEmbedded(true);
        container.init();
        container.start();
        return container;
    }

}
