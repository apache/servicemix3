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
package org.apache.servicemix.jbi.management;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import junit.framework.TestCase;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.junit.Test;

/**
 * Additional tests to ensure the {@link ManagementContext} behaves properly when the actual ObjectName does not match
 * the ObjectName used for registration (e.g. on WebSphere, where cell/node/process information gets appended to the ObjectName)
 */
public class WebSphereManagementContextTest extends TestCase {

    private static final ObjectName OBJECT_NAME =
            ManagementContext.getSystemObjectName(ManagementContext.DEFAULT_DOMAIN, JBIContainer.DEFAULT_NAME, SampleMBean.class);

    @Test
    public void testObjectNameAltered() throws Exception {
        ManagementContext context = new ManagementContext();
        MBeanServer server = MBeanServerFactory.createMBeanServer();
        context.setMBeanServer(server);

        SampleMBeanImpl impl = new SampleMBeanImpl();
        RenamingStandardMBean mbean = new RenamingStandardMBean(impl, SampleMBean.class);

        // let's register and unregister the MBean
        context.registerMBean(OBJECT_NAME, impl, mbean);
        context.unregisterMBean(impl);

        // if the previous unregistration failed because of non-matching object names, the method below will throw
        // javax.management.InstanceAlreadyExistsException
        context.registerMBean(OBJECT_NAME, impl, mbean);
    }


    /*
     * MBean interface definition used for testing
     */
    public static interface SampleMBean {

        void doSomething();

    }

    /*
     * MBean implementation
     */
    public static final class SampleMBeanImpl implements SampleMBean {

        public void doSomething() {
            // graciously do nothing here
        }
    }

    /*
     * {@link StandardMBean} implementation that will append parts to the MBean's object name upon registration
     * (i.e. adding node/cell/process parts similar to what WebSphere does)
     */
    public final class RenamingStandardMBean extends StandardMBean implements MBeanRegistration {


        public <T> RenamingStandardMBean(T object, Class<T> type) throws NotCompliantMBeanException {
            super(object, type);
        }


        public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
            return new ObjectName(objectName.toString() + ",node=node1,cell=cell1,process=process1");
        }

        public void postRegister(Boolean aBoolean) {
            // graciously do nothing here
        }

        public void preDeregister() throws Exception {
            // graciously do nothing here
        }

        public void postDeregister() {
            // graciously do nothing here
        }
    }
}
