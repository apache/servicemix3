/*
 * Created on Jun 15, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package org.servicemix.jbi.management;
import org.servicemix.jbi.container.JBIContainer;

import javax.jbi.management.AdminServiceMBean;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * ManagementContextTest
 */
public class AdminServiceTest extends TestCase {
    JBIContainer container;

    protected void setUp() throws Exception {
    	container = new JBIContainer();
    	container.setCreateMBeanServer(true);
    	container.init();
    }
    
    protected void tearDown() throws Exception {
        container.shutDown();
    }

    public void testAdminService() throws Exception {
        // The host, port and path where the rmiregistry runs.
        String namingHost = "localhost";
        int namingPort = 1099;
        String jndiPath = "/" + JBIContainer.DEFAULT_NAME + "JMX";
        // The address of the connector server
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
                + namingHost + ":" + namingPort + jndiPath);
        // Connect a JSR 160 JMXConnector to the server side
        JMXConnector connector = JMXConnectorFactory.connect(url);
        // Retrieve an MBeanServerConnection that represent the MBeanServer the remote
        // connector server is bound to
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        // Call the server side as if it is a local MBeanServer
        ObjectName asmName = getObjectName(ManagementContext.class);
        Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, asmName,
        		AdminServiceMBean.class, true);
        AdminServiceMBean asm = (AdminServiceMBean) proxy;

        System.out.println(asm.getBindingComponents()); 
        System.out.println(asm.getComponentByName("toto")); 
    }
    
    protected  ObjectName getObjectName (Class systemClass){
        return ManagementContext.getSystemObjectName(ManagementContext.DEFAULT_DOMAIN, JBIContainer.DEFAULT_NAME, systemClass);
    }
}
