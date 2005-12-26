/*
 * Created on Jun 15, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package org.apache.servicemix.jbi.management;
import org.apache.servicemix.jbi.container.JBIContainer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

/**
 * ManagementContextTest
 */
public class ManagementAttributesTest extends TestCase {
    JBIContainer container;

    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setCreateMBeanServer(true);
        container.init();
        container.start();
    }
    
    protected void tearDown() throws Exception {
        container.shutDown();
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
        
        System.out.println(connection.getMBeanCount());


        Set set = connection.queryNames(new ObjectName(connection.getDefaultDomain() + ":*"), null);
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName)iter.next();
            System.out.println(name.toString());
            MBeanInfo info = connection.getMBeanInfo(name);
            MBeanAttributeInfo[] mia = info.getAttributes();
            String[] attrNames = new String[mia.length];
            for (int i = 0; i < mia.length; i++) {
                attrNames[i] = mia[i].getName();
                System.out.println("attr " + mia[i].getName() + " " + mia[i].getType() + " " + connection.getAttribute(name,mia[i].getName()));
            }

            AttributeList attributeList = (AttributeList) connection.getAttributes(name,attrNames);
            for (int i = 0; i < attributeList.size(); i++) {
                Attribute attribute = (Attribute) attributeList.get(i);
                System.out.println("bulk " + attribute.getName() + " " + attribute.getValue() + " " + attribute.toString());
            }

        }

        
    }
    
}
