package org.apache.servicemix.itests.deadlock;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class DeadlockTest extends TestSupport {
    
    public void setUp() throws Exception {
        super.setUp();
        messageCount = 100;
    }

    public void test() throws Exception {
        ConnectionFactory cf = (ConnectionFactory) getBean("connectionFactory");
        Connection con = cf.createConnection();
        Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("topic/A");
        MessageProducer producer = session.createProducer(topic);
        for (int i = 0; i < messageCount; i++) {
            TextMessage txt = session.createTextMessage();
            txt.setText("<hello>" + i + "</hello>");
            producer.send(txt);
        }
        System.out.println("Message sent");
        
        Receiver receiver = (Receiver) getBean("receiver");
        receiver.getMessageList().waitForMessagesToArrive(messageCount, 60000);
        assertEquals("expected number of messages", messageCount, receiver.getMessageList().getMessageCount());
    }

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/itests/deadlock/servicemix-deadlock.xml");
    }
    
}
