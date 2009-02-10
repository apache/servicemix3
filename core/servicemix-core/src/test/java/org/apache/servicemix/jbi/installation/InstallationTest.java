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

import javax.jbi.component.Bootstrap;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.management.InstallerMBean;
import javax.jbi.management.LifeCycleMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.easymock.MockControl;

/**
 * 
 * JbiTaskTest
 * 
 * @version $Revision$
 */
public class InstallationTest extends AbstractManagementTest {

    /**
     * Installer should not be persistent across restart
     * 
     * @throws Exception
     */
    public void testLoadNewInstallerAndRestart() throws Exception {
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);

        // configure bootstrap
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrapMock.replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        assertNotNull(Bootstrap1.getInstallContext());
        assertTrue(Bootstrap1.getInstallContext().isInstall());
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        shutdownContainer();
        // check mocks
        bootstrapMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // test container start
        startContainer(false);
        // check mocks
        bootstrapMock.verify();
    }

    /**
     * Installer should not be persistent across restart
     * 
     * @throws Exception
     */
    public void testLoadNewInstallerAndLoadNewInstaller() throws Exception {
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);

        // configure bootstrap
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrapMock.replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        assertNotNull(Bootstrap1.getInstallContext());
        assertTrue(Bootstrap1.getInstallContext().isInstall());
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                        installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        // check mocks
        bootstrapMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // test load new installer
        try {
            getInstallationService().loadNewInstaller(installJarUrl);
            fail("Expected an exception");
        } catch (Exception e) {
            // ok, this should fail
        }
        // check mocks
        bootstrapMock.verify();
    }

    /**
     * Installer is created, component installed and server restarted
     * 
     * @throws Exception
     */
    public void testInstallAndRestart() throws Exception {
        // Create mocks
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        ExtMockControl componentMock = ExtMockControl.createControl(Component.class);
        Component component = (Component) componentMock.getMock();
        Component1.setDelegate(component);

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
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
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // unload installer
        container.getInstallationService().unloadInstaller("component1", false);
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // shutdown container
        shutdownContainer();
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // start container
        startContainer(false);
        lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName,
                        LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.SHUTDOWN, lifecycleMBean.getCurrentState());
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
    }

    /**
     * Installer is created, component installed, started and server restarted
     * 
     * @throws Exception
     */
    public void testInstallStartAndRestart() throws Exception {
        // Create mocks
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        ExtMockControl componentMock = ExtMockControl.createControl(Component.class);
        Component component = (Component) componentMock.getMock();
        Component1.setDelegate(component);
        ExtMockControl lifecycleMock = ExtMockControl.createControl(ComponentLifeCycle.class);
        ComponentLifeCycle lifecycle = (ComponentLifeCycle) lifecycleMock.getMock();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // configure lifecycle
        lifecycleMock.reset();
        lifecycleMock.replay();
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
        bootstrapMock.verify();
        componentMock.verify();
        lifecycleMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        component.getLifeCycle();
        componentMock.setReturnValue(lifecycle);
        componentMock.replay();
        // configure lifecycle
        lifecycleMock.reset();
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        lifecycleMock.replay();
        // test component installation
        lifecycleMBean.start();
        assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
        lifecycleMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // configure lifecycle
        lifecycleMock.reset();
        lifecycle.stop();
        lifecycle.shutDown();
        lifecycleMock.replay();
        // shutdown container
        shutdownContainer();
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
        lifecycleMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        component.getLifeCycle();
        componentMock.setDefaultReturnValue(lifecycle);
        componentMock.replay();
        // configure lifecycle
        lifecycleMock.reset();
        lifecycle.getExtensionMBeanName();
        lifecycleMock.setDefaultReturnValue(null);
        lifecycle.init(null);
        lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
        lifecycle.start();
        lifecycleMock.replay();
        // start container
        startContainer(false);
        lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName,
                        LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
        lifecycleMock.verify();
    }

    /**
     * Installer is created, component installed. Then we unload the installer
     * and reload it.
     * 
     * @throws Exception
     */
    public void testInstallAndReloadInstaller() throws Exception {
        // Create mocks
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        ExtMockControl componentMock = ExtMockControl.createControl(Component.class);
        Component component = (Component) componentMock.getMock();
        Component1.setDelegate(component);
        ExtMockControl lifecycleMock = ExtMockControl.createControl(ComponentLifeCycle.class);
        ComponentLifeCycle lifecycle = (ComponentLifeCycle) lifecycleMock.getMock();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        component.getLifeCycle();
        componentMock.setDefaultReturnValue(lifecycle);
        componentMock.replay();
        // configure lifecycle
        lifecycleMock.reset();
        lifecycleMock.replay();
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
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // unload installer
        container.getInstallationService().unloadInstaller("component1", false);
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // shutdown container
        installerName = container.getInstallationService().loadInstaller("component1");
        assertNotNull(installerName);
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
    }

    /**
     * Installer is created, component installed, uninstalled and reinstalled
     * 
     * @throws Exception
     */
    public void testInstallAndReinstall() throws Exception {
        // Create mocks
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        ExtMockControl componentMock = ExtMockControl.createControl(Component.class);
        Component component = (Component) componentMock.getMock();
        Component1.setDelegate(component);

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
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
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.onUninstall();
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // unload installer
        container.getInstallationService().unloadInstaller("component1", true);
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.getExtensionMBeanName();
        bootstrapMock.setReturnValue(null);
        bootstrap.onInstall();
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // test component installation
        startContainer(true);
        installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        installerName = getInstallationService().loadNewInstaller(installJarUrl);
        installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), installerName,
                        InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        lifecycleName = installer.install();
        lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName,
                        LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.SHUTDOWN, lifecycleMBean.getCurrentState());
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();

        // configure bootstrap
        bootstrapMock.reset();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // shutdown container
        shutdownContainer();
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
    }

}
