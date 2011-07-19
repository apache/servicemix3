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
package org.apache.servicemix.jbi.installation;

import java.io.File;

import javax.jbi.component.Bootstrap;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentServiceMBean;
import javax.jbi.management.InstallerMBean;
import javax.jbi.management.LifeCycleMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.easymock.MockControl;

public class DeploymentTest extends AbstractManagementTest {

    // Create mocks
    protected ExtMockControl bootstrapMock;

    protected Bootstrap bootstrap;

    protected ExtMockControl componentMock;

    protected Component component;

    protected ExtMockControl lifecycleMock;

    protected ComponentLifeCycle lifecycle;

    protected ExtMockControl managerMock;

    protected ServiceUnitManager manager;

    protected void setUp() throws Exception {
        super.setUp();
        // Create mocks
        bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        componentMock = ExtMockControl.createControl(Component.class);
        component = (Component) componentMock.getMock();
        Component1.setDelegate(component);
        lifecycleMock = ExtMockControl.createControl(ComponentLifeCycle.class);
        lifecycle = (ComponentLifeCycle) lifecycleMock.getMock();
        managerMock = ExtMockControl.createControl(ServiceUnitManager.class);
        manager = (ServiceUnitManager) managerMock.getMock();
    }

    protected void reset() {
        bootstrapMock.reset();
        componentMock.reset();
        lifecycleMock.reset();
        managerMock.reset();
    }

    protected void replay() {
        bootstrapMock.replay();
        componentMock.replay();
        lifecycleMock.replay();
        managerMock.replay();
    }

    protected void verify() {
        bootstrapMock.verify();
        componentMock.verify();
        lifecycleMock.verify();
        managerMock.verify();
    }

    /**
     * SA is deployed and started
     * 
     * @throws Exception
     */
    public void testDeployAndStart() throws Exception {
        // configure mocks
        reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        ObjectName lifecycleName = installer.install();
        LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        lifecycleName, LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.SHUTDOWN, lifecycleMBean.getCurrentState());
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        replay();
        // test component installation
        lifecycleMBean.start();
        assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        manager.deploy(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        managerMock.setReturnValue(null);
        replay();
        // deploy sa
        assertTrue(getDeploymentService().canDeployToComponent("component1"));
        File installSaUrl = createServiceAssemblyArchive("sa", "su", "component1");
        String result = getDeploymentService().deploy(installSaUrl.getAbsolutePath());
        LOGGER.debug(result);
        String[] sas = getDeploymentService().getDeployedServiceAssemblies();
        assertNotNull(sas);
        assertEquals(1, sas.length);
        assertEquals("sa", sas[0]);
        sas = getDeploymentService().getDeployedServiceAssembliesForComponent("component1");
        assertNotNull(sas);
        assertEquals(1, sas.length);
        assertEquals("sa", sas[0]);
        assertEquals(DeploymentServiceMBean.SHUTDOWN, getDeploymentService().getState("sa"));
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ZERO_OR_MORE);
        manager.init(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        manager.start("su");
        replay();
        // start sa
        getDeploymentService().start("sa");
        assertEquals(DeploymentServiceMBean.STARTED, getDeploymentService().getState("sa"));
        // check mocks
        verify();

        // Clean shutdown
        reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        lifecycle.stop();
        lifecycle.shutDown();
        // manager.stop("su");
        manager.shutDown("su");
        replay();
        shutdownContainer();
    }

    /**
     * SA is deployed and started
     * 
     * @throws Exception
     */
    public void testDeployAndRestart() throws Exception {
        // configure mocks
        reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        ObjectName lifecycleName = installer.install();
        LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        lifecycleName, LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.SHUTDOWN, lifecycleMBean.getCurrentState());
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        replay();
        // test component installation
        lifecycleMBean.start();
        assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        manager.deploy(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        managerMock.setReturnValue(null);
        replay();
        // deploy sa
        assertTrue(getDeploymentService().canDeployToComponent("component1"));
        File installSaUrl = createServiceAssemblyArchive("sa", "su", "component1");
        getDeploymentService().deploy(installSaUrl.getAbsolutePath());
        String[] sas = getDeploymentService().getDeployedServiceAssemblies();
        assertNotNull(sas);
        assertEquals(1, sas.length);
        assertEquals("sa", sas[0]);
        sas = getDeploymentService().getDeployedServiceAssembliesForComponent("component1");
        assertNotNull(sas);
        assertEquals(1, sas.length);
        assertEquals("sa", sas[0]);
        assertEquals(DeploymentServiceMBean.SHUTDOWN, getDeploymentService().getState("sa"));
        // check mocks
        verify();

        // configure mocks
        reset();
        lifecycle.stop();
        lifecycle.shutDown();
        replay();
        // shutdown container
        shutdownContainer();
        // check mocks
        verify();

        // configure mocks
        reset();
        // XXX Should the bootstrap re-init?
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        manager.init(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        manager.shutDown("su");
        replay();
        // start container
        startContainer(false);
        // check mocks
        verify();

        // Clean shutdown
        reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        lifecycle.stop();
        lifecycle.shutDown();
        // manager.stop("su");
        manager.shutDown("su");
        replay();
        shutdownContainer();
    }

    /**
     * SA is deployed and started
     * 
     * @throws Exception
     */
    public void testDeployStartAndRestart() throws Exception {
        // configure mocks
        reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        ObjectName lifecycleName = installer.install();
        LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        lifecycleName, LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.SHUTDOWN, lifecycleMBean.getCurrentState());
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        replay();
        // test component installation
        lifecycleMBean.start();
        assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
        // check mocks
        verify();

        // configure mocks
        reset();
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        manager.deploy(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        managerMock.setReturnValue(null);
        replay();
        // deploy sa
        assertTrue(getDeploymentService().canDeployToComponent("component1"));
        File installSaUrl = createServiceAssemblyArchive("sa", "su", "component1");
        getDeploymentService().deploy(installSaUrl.getAbsolutePath());
        String[] sas = getDeploymentService().getDeployedServiceAssemblies();
        assertNotNull(sas);
        assertEquals(1, sas.length);
        assertEquals("sa", sas[0]);
        sas = getDeploymentService().getDeployedServiceAssembliesForComponent("component1");
        assertNotNull(sas);
        assertEquals(1, sas.length);
        assertEquals("sa", sas[0]);
        assertEquals(DeploymentServiceMBean.SHUTDOWN, getDeploymentService().getState("sa"));
        // check mocks
        verify();

        // configure mocks
        reset();
        manager.init(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        manager.start("su");
        replay();
        // start sa
        getDeploymentService().start("sa");
        assertEquals(DeploymentServiceMBean.STARTED, getDeploymentService().getState("sa"));
        // check mocks
        verify();

        // configure mocks
        reset();
        lifecycle.stop();
        lifecycle.shutDown();
        manager.stop("su");
        manager.shutDown("su");
        replay();
        // shutdown container
        shutdownContainer();
        // check mocks
        verify();

        // configure mocks
        reset();
        // XXX Should the bootstrap re-init?
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        manager.init(null, null);
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        manager.start("su");
        managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
        replay();
        // start container
        startContainer(false);
        // check mocks
        verify();

        // Clean shutdown
        reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle, MockControl.ONE_OR_MORE);
        component.getServiceUnitManager();
        componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
        lifecycle.stop();
        lifecycle.shutDown();
        // manager.stop("su");
        manager.shutDown("su");
        replay();
        shutdownContainer();
    }

}
