package org.servicemix.ws.notification.invoke;

import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.servicemix.jbi.jaxp.BytesSource;
import org.servicemix.jbi.jaxp.StringSource;
import org.servicemix.wspojo.notification.NotificationConsumer;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses an instance of {@link NotificationConsumer} to invoke the notification
 */
public class NotificationConsumerInvoker extends InvokerSupport implements MessageListener {

    private NotificationConsumer notificationConsumer;

    public NotificationConsumerInvoker(NotificationConsumer notificationConsumer) {
        this.notificationConsumer = notificationConsumer;
    }

    protected void dispatchMessage(TopicExpressionType topic, Message message) throws JMSException, IOException {
        if (message instanceof BytesMessage) {
            BytesMessage bm = (BytesMessage) message;
            byte data[] = new byte[(int) bm.getBodyLength()];
            bm.readBytes(data);
            dispatch(topic, createXml(data));
        }
        else if (message instanceof TextMessage) {
            TextMessage tm = (TextMessage) message;
            String text = tm.getText();
            dispatch(topic, createXml(text));
        }
    }

    protected void dispatch(TopicExpressionType topic, Object body) throws RemoteException {
        NotificationMessageHolderType messageHolder = new NotificationMessageHolderType();
        EndpointReferenceType producerReference = getProducerReference();
        if (producerReference != null) {
            messageHolder.setProducerReference(producerReference);
        }
        messageHolder.setTopic(topic);
        messageHolder.setMessage(body);

        List<NotificationMessageHolderType> list = new ArrayList<NotificationMessageHolderType>(1);
        list.add(messageHolder);
        notificationConsumer.notify(list);
    }

    /**
     * Factory method to turn XML as bytes into some POJO which by default will
     * create a {@link BytesSource}
     */
    protected Object createXml(byte[] xmlBytes) {
        return new BytesSource(xmlBytes);
    }

    /**
     * Factory method to turn XML as a String into some POJO which by default
     * will create a {@link StringSource}
     */
    protected Object createXml(String xmlText) {
        return new StringSource(xmlText);
    }

}
