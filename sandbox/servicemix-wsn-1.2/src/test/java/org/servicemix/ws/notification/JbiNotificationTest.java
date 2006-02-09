package org.servicemix.ws.notification;

import org.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Node;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import java.util.Iterator;
import java.util.List;

public class JbiNotificationTest extends TestSupport {

    public void testSubscribe() throws Exception {
        QName serviceName = new QName("http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.wsdl", "JBINotificationBroker");

        String file = "wsn-subscribe.xml";
        Object answer = requestServiceWithFileRequest(serviceName, file);
        if (answer instanceof Source) {
            System.err.println(transformer.toString((Source) answer));
            answer = transformer.toDOMNode((Source) answer);
        }
        assertTrue("Should return a DOM Node: " + answer, answer instanceof Node);
        Node node = (Node) answer;
        System.out.println(transformer.toString(node));

        file = "wsn-notify.xml";
        sendServiceWithFileRequest(serviceName, file);
        
        MyReceiver consumer = (MyReceiver) getBean("consumer");
        assertMessagesReceived(consumer.getMessageList(), 1);
    }
    
    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/servicemix/ws/notification/jbi-wsn.xml");
    }
    
    protected void assertMessagesReceived(MessageList messageList, int messageCount) throws Exception {
        messageList.assertMessagesReceived(messageCount);
        List list = messageList.getMessages();
        int counter = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            NormalizedMessage message = (NormalizedMessage) iter.next();
            log.info("Message " + (counter++) + " is: " + message);
            log.info(transformer.contentToString(message));
        }
    }
}
