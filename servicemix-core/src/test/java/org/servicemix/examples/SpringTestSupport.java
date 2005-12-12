package org.servicemix.examples;

import org.servicemix.components.util.ComponentAdaptor;
import org.servicemix.jbi.container.SpringJBIContainer;
import org.servicemix.jbi.framework.LocalComponentConnector;
import org.servicemix.tck.MessageList;
import org.servicemix.tck.Receiver;
import org.servicemix.tck.Sender;
import org.springframework.context.support.AbstractXmlApplicationContext;

import javax.jbi.component.Component;

import junit.framework.TestCase;

/**
 * A base class for spring related test cases
 *
 * @version $Revision$
 */
public abstract class SpringTestSupport extends TestCase {
    protected SpringJBIContainer jbi;
    protected AbstractXmlApplicationContext context;
    protected int messageCount = 1;

    protected void setUp() throws Exception {

        context = createBeanFactory();
        //context.setXmlValidating(false);

        // lets force the JBI container to be constructed first
        jbi = (SpringJBIContainer) context.getBean("jbi");
        assertNotNull("JBI Container not found in spring!", jbi);

    }

    protected abstract AbstractXmlApplicationContext createBeanFactory();

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        Sender sender = getSender();
        assertNotNull(sender);
        assertNotNull(getReceiver());

        sender.sendMessages(messageCount);

        MessageList messageList = getReceivedMessageList();
        messageList.assertMessagesReceived(messageCount);
    }

    protected Sender getSender() {
        Object cmp = getComponent("sender");
        if (cmp instanceof ComponentAdaptor) {
            cmp = ((ComponentAdaptor) cmp).getLifeCycle();
        }
        return (Sender) cmp;
    }

    protected Receiver getReceiver() {
        Object cmp = getComponent("receiver");
        if (cmp instanceof ComponentAdaptor) {
            cmp = ((ComponentAdaptor) cmp).getLifeCycle();
        }
        return (Receiver) cmp;
    }

    protected MessageList getReceivedMessageList() {
        return getReceiver().getMessageList();
    }
    
    protected Component getComponent(String name) {
        LocalComponentConnector lcc = jbi.getLocalComponentConnector(name);
        return lcc != null ? lcc.getComponent() : null;
    }

    protected Object getBean(String name) {
        Object value = jbi.getBean(name);
        if (value == null) {
            value = context.getBean(name);
        }
        assertNotNull(name + " not found in JBI container!", value);
        return value;
    }
}
