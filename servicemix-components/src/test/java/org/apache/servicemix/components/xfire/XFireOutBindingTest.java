package org.apache.servicemix.components.xfire;

import javax.jbi.JBIException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.xbean.spring.context.ClassPathXmlApplicationContext;

public class XFireOutBindingTest extends TestSupport {
    private OneWayService receiverService;

    protected void setUp() throws Exception {
        super.setUp();
        
        receiverService = (OneWayService) getBean("xfireReceiverService");
    }
    
    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        sendFile(new QName("http://xfire.components.servicemix.org", "OneWayService"),
                "/org/apache/servicemix/components/xfire/oneway.xml"); 

        receiverService.getMessageList().assertMessagesReceived(1);
    }

    protected void sendFile(QName serviceName, String fileOnClassPath) throws JBIException {
        Source content = getSourceFromClassPath(fileOnClassPath);

        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(serviceName);

        client.send(resolver, null, null, content);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(new String[] {
                "/org/apache/servicemix/components/xfire/xfire-out.xml",
                "/org/codehaus/xfire/spring/xfire.xml"
        });
    }
}
