/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/


package org.apache.servicemix.jbi.installation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.util.FileUtil;
import org.easymock.MockControl;

import javax.jbi.component.Bootstrap;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.management.AdminServiceMBean;
import javax.jbi.management.InstallationServiceMBean;
import javax.jbi.management.InstallerMBean;
import javax.jbi.management.LifeCycleMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;


/**
 *
 * JbiTaskTest
 * @version $Revision$
 */
public class InstallationTest extends TestCase {
    
    private static Log logger = LogFactory.getLog(InstallationTest.class);
    
    protected JBIContainer container;
   
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            shutdownContainer();
        } catch (Exception e) {
            logger.info("Error shutting down container", e);
        }
    }
    
    protected void startContainer(boolean clean) throws Exception {
        shutdownContainer();
        if (clean) {
            FileUtil.deleteFile(new File("testWDR"));
        }
        container = new JBIContainer();
        container.setCreateMBeanServer(true);
        container.setMonitorInstallationDirectory(false);
        container.setRootDir("testWDR");
        container.init();
        container.start();
    }
    
    protected void shutdownContainer() throws Exception {
        if (container != null) {
            container.shutDown();
        }
    }
    
    protected File createInstallerArchive(String jbi) throws Exception {
        InputStream is = getClass().getResourceAsStream(jbi + "-jbi.xml");
        File jar = File.createTempFile("jbi", "zip");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar));
        jos.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        jos.write(buffer);
        jos.closeEntry();
        jos.close();
        is.close();
        return jar;
    }
    
    protected InstallationServiceMBean getInstallationService() throws Exception {
        return container.getInstallationService();
    }
    
    protected AdminServiceMBean getAdminService() throws Exception {
        return container.getManagementContext();
    }
    
    
    /**
     * Installer should not be persistent across restart
     * @throws Exception
     */
    public void testLoadNewInstallerAndRestart() throws Exception {
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        
        // configure bootstrap
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrapMock.replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        assertNotNull(Bootstrap1.getInstallContext());
        assertTrue(Bootstrap1.getInstallContext().isInstall());
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), installerName, InstallerMBean.class, false);
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
     * @throws Exception
     */
    public void testLoadNewInstallerAndLoadNewInstaller() throws Exception {
        ExtMockControl bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) bootstrapMock.getMock();
        Bootstrap1.setDelegate(bootstrap);
        
        // configure bootstrap
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrapMock.replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        assertNotNull(Bootstrap1.getInstallContext());
        assertTrue(Bootstrap1.getInstallContext().isInstall());
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), installerName, InstallerMBean.class, false);
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
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        ObjectName lifecycleName = installer.install();
        LifeCycleMBean lifecycleMBean = (LifeCycleMBean)  MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName, LifeCycleMBean.class, false);
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
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // start container
        startContainer(false);
        lifecycleMBean = (LifeCycleMBean)  MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName, LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.SHUTDOWN, lifecycleMBean.getCurrentState());
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
    }

    /**
     * Installer is created, component installed, started and server restarted
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
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        ObjectName lifecycleName = installer.install();
        LifeCycleMBean lifecycleMBean = (LifeCycleMBean)  MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName, LifeCycleMBean.class, false);
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
        assertEquals(LifeCycleMBean.RUNNING, lifecycleMBean.getCurrentState());
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
        // start container
        startContainer(false);
        lifecycleMBean = (LifeCycleMBean)  MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName, LifeCycleMBean.class, false);
        assertEquals(LifeCycleMBean.RUNNING, lifecycleMBean.getCurrentState());
        // check mocks
        bootstrapMock.verify();
        componentMock.verify();
        lifecycleMock.verify();
    }

    /**
     * Installer is created, component installed.
     * Then we unload the installer and reload it.
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
        
        // configure bootstrap
        bootstrapMock.reset();
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
        bootstrap.onInstall();
        bootstrap.cleanUp();
        bootstrapMock.replay();
        // configure component
        componentMock.reset();
        componentMock.replay();
        // test component installation
        startContainer(true);
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        ObjectName installerName = getInstallationService().loadNewInstaller(installJarUrl);
        InstallerMBean installer = (InstallerMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), installerName, InstallerMBean.class, false);
        assertFalse(installer.isInstalled());
        ObjectName lifecycleName = installer.install();
        LifeCycleMBean lifecycleMBean = (LifeCycleMBean)  MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(), lifecycleName, LifeCycleMBean.class, false);
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
        bootstrap.init(null);
        bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
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

}
