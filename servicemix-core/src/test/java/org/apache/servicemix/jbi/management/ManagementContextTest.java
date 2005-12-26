/*
 * Created on Jun 15, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package org.apache.servicemix.jbi.management;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.management.ManagementContext;

import javax.jbi.management.LifeCycleMBean;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * ManagementContextTest
 */
public class ManagementContextTest extends TestCase {
    ManagementContext context;

    protected void setUp() throws Exception {
        context = new ManagementContext();
        context.setCreateMBeanServer(true);
        context.init(new JBIContainer(), null);
    }
    
    protected void tearDown() throws Exception {
        context.shutDown();
    }

    public void testRemote() throws Exception {
        //      The JMXConnectorServer protocol, in this case is RMI.
        String serverProtocol = "rmi";
        // The RMI server's host: this is actually ignored by JSR 160
        // since this information is stored in the RMI stub.
        
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
        ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
        Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, delegateName,
                MBeanServerDelegateMBean.class, true);
        MBeanServerDelegateMBean delegate = (MBeanServerDelegateMBean) proxy;
        // The magic of JDK 1.3 dynamic proxy and JSR 160:
        // delegate.getImplementationVendor() is actually a remote JMX call,
        // but it looks like a local, old-style, java call.
        System.out.println(delegate.getImplementationVendor() + " is cool !");
        
        ObjectName objName = context.createObjectName(context);
        
        
        proxy = MBeanServerInvocationHandler.newProxyInstance(connection, objName,
                LifeCycleMBean.class, true);
        LifeCycleMBean mc = (LifeCycleMBean) proxy;
        System.out.println("STATE = " + mc.getCurrentState());
        mc.start();
        System.out.println("STATE = " + mc.getCurrentState());
        mc.stop();
        System.out.println("STATE = " + mc.getCurrentState());
        mc.shutDown();
    }
    
}
