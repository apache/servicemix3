/**
 *
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.servicemix.ws.notification;

import EDU.oswego.cs.dl.util.concurrent.Slot;

import org.activemq.ActiveMQConnection;
import org.activemq.ActiveMQConnectionFactory;
import org.activemq.command.ActiveMQTopic;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.servicemix.wspojo.notification.NotificationConsumer;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$
 */
public class ActiveMQNotificationBrokerTest extends NotificationTestSupport {

    /*
     * TODO public void testGetRequiresRegistrationProperty() throws Exception {
     * ActiveMQNotificationBroker broker = new ActiveMQNotificationBroker();
     * 
     * GetResourcePropertyDocument request =
     * GetResourcePropertyDocument.Factory.newInstance(); QName property = new
     * QName("http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd",
     * "RequiresRegistration"); request.setGetResourceProperty( property );
     * System.out.println(request); GetResourcePropertyResponseDocument response =
     * broker.getResourceProperty(null, request); System.out.println(response);
     * assertNotNull(response); XmlCursor cursor = response.newCursor();
     * cursor.toChild(property); cursor.toFirstContentToken();
     * assertEquals("false", cursor.getTextValue()); }
     */

    public void testSendNotify() throws Exception {

        ActiveMQNotificationBroker broker = new ActiveMQNotificationBroker();
        ActiveMQConnection connection = broker.getConnection();
        Session session = connection.createSession(false, 0);
        ActiveMQTopic topic = new ActiveMQTopic("Test");
        MessageConsumer consumer = session.createConsumer(topic);

        NotificationMessageHolderType messageHolder = new NotificationMessageHolderType();
        messageHolder.setTopic(topicConverter.toTopicExpression(topic));
        messageHolder.setMessage(createMessage());

        List<NotificationMessageHolderType> list = new ArrayList<NotificationMessageHolderType>();
        list.add(messageHolder);

        broker.notify(list);

        System.out.println(list);

        Message message = consumer.receive(3000);
        assertNotNull(message);

        System.out.println("Received inbound message: " + message);
    }

    public void testSubscribe() throws Exception {

        Slot result = new Slot();
        ActiveMQNotificationBroker broker = createBroker(result);

        addSubscription(broker);
        sendNotification(broker);

        List<NotificationMessageHolderType> notifyMessages = (List<NotificationMessageHolderType>) result.poll(2000);
        System.out.println("Got Notify: " + notifyMessages);

        assertValidMessage(notifyMessages);
    }

    public void testSubscriptionPauseResume() throws Exception {

        Slot result = new Slot();
        ActiveMQNotificationBroker broker = createBroker(result);

        EndpointReferenceType subRef = addSubscription(broker);

        // The sub should be running and we should be getting notifed now.
        sendNotification(broker);

        List<NotificationMessageHolderType> subNotifyDoc = (List<NotificationMessageHolderType>) result.poll(2000);
        assertNotNull(subNotifyDoc);

        // Pause the subscription.
        broker.getSubscriptionManager().pauseSubscription(subRef);

        // The sub should be stopped and we should not be getting notifed now.
        sendNotification(broker);
        subNotifyDoc = (List<NotificationMessageHolderType>) result.poll(2000);
        assertNull(subNotifyDoc);

        // Resume the subscription.
        broker.getSubscriptionManager().resumeSubscription(subRef);

        // We should now get the message that was previously sent since the sub
        // is now running.
        subNotifyDoc = (List<NotificationMessageHolderType>) result.poll(2000);
        assertNotNull(subNotifyDoc);
    }

    protected ActiveMQNotificationBroker createBroker(final Slot result) throws JMSException {
        ActiveMQNotificationBroker broker = new ActiveMQNotificationBroker() {
            protected NotificationConsumer createNotificationConsumer(final ActiveMQSubscription consumerReference) {
                return new StubNotificationConsumer(result);
            }
        };
        return broker;
    }
}
