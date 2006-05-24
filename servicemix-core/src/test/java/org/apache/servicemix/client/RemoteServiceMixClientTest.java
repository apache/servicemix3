package org.apache.servicemix.client;

import java.util.Set;

import org.apache.activemq.transport.vm.VMTransportFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class RemoteServiceMixClientTest extends ServiceMixClientTest {

    protected void tearDown() throws Exception {
        ((RemoteServiceMixClient) client).shutDown();
        super.tearDown();
        Set servers = VMTransportFactory.servers.keySet();
        String[] serverNames = (String[]) servers.toArray(new String[0]);
        for (int i = 0; i < serverNames.length; i++) {
            VMTransportFactory.stopped(serverNames[i]);
        }
    }
    
    protected ServiceMixClient getClient() throws Exception {
        /*
        RemoteServiceMixClient client = new RemoteServiceMixClient("tcp://localhost:61616");
        client.start();
        return client;
        */
        return super.getClient();
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/client/remote.xml");
    }

    /*
    public void testSendUsingMapAndPOJOsUsingContainerRouting() throws Exception {
    }

    public void testRequestUsingPOJOWithXStreamMarshaling() throws Exception {
    }
    */

}
