package org.apache.servicemix.components.xfire;

import javax.xml.namespace.QName;

import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Node;
import org.xbean.spring.context.ClassPathXmlApplicationContext;

public class XFireBindingTest extends TestSupport {

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        Object answer = requestServiceWithFileRequest(new QName("http://xfire.components.servicemix.org", "Echo"),
                "/org/servicemix/components/xfire/echo.xml");
        assertTrue("Shoud return a DOM Node: " + answer, answer instanceof Node);
        Node node = (Node) answer;
        System.out.println(transformer.toString(node));
        
        Echo echo = (Echo) context.getBean("xfireReceiverService");
        assertEquals(1, echo.getCount());
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(new String[] {
                "/org/apache/servicemix/components/xfire/xfire-inout.xml",
                "/org/codehaus/xfire/spring/xfire.xml"
        });
    }
}
