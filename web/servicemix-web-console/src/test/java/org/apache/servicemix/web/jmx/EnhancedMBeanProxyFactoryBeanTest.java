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
package org.apache.servicemix.web.jmx;

import junit.framework.TestCase;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Test cases for {@link EnhancedMBeanProxyFactoryBean}
 */
public class EnhancedMBeanProxyFactoryBeanTest extends TestCase {

    public void testWebSphereAddsCellNodeAndProcess() throws Exception {
        MBeanServer server = MBeanServerFactory.createMBeanServer(EnhancedMBeanProxyFactoryBean.WEBSPHERE);
        server.registerMBean(new Mock(), new ObjectName("WebSphere:name=JVM,cell=myCell,process=myProcess,node=myNode"));

        Mock mbean = new Mock();
        server.registerMBean(mbean, new ObjectName("test:name=MyMockBean,cell=myCell,process=myProcess,node=myNode"));

        EnhancedMBeanProxyFactoryBean bean = new EnhancedMBeanProxyFactoryBean();
        bean.setServer(server);
        bean.setObjectName("test:name=MyMockBean");
        bean.setProxyInterface(MockMBean.class);
        bean.afterPropertiesSet();

        MockMBean proxy = (MockMBean) bean.getObject();
        proxy.doSomethingUseful();
        assertEquals("We should have use the MBean using the object name without the cell/process/node inforamtion",
                     1, mbean.usefulness);
    }

    public static interface MockMBean {

        public void doSomethingUseful();

    }

    public static final class Mock implements MockMBean {

        private int usefulness;

        public void doSomethingUseful() {
            usefulness++;
        }

    }
}
