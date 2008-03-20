/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.notification;

import org.activemq.command.ActiveMQTopic;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryExpressionType;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public abstract class NotificationTestSupport extends TestCase {
    protected final String TOPIC_NAME = getClass() + "." + getName();

    protected ActiveMQTopic topic = new ActiveMQTopic(TOPIC_NAME);
    protected TopicExpressionConverter topicConverter = new TopicExpressionConverter();

    protected EndpointReferenceType addSubscription(ActiveMQNotificationBroker broker) throws Exception {
        // START SNIPPET: subscribe
        String topicName = TOPIC_NAME;
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.getContent().add(topicName);
        EndpointReferenceType consumerReference = createReference();
        Boolean useNotify = Boolean.TRUE;
        QueryExpressionType precondition = null;
        QueryExpressionType selector = null;
        Object subscriptionPolicy = null;
        XMLGregorianCalendar terminationTime = null;
        EndpointReferenceType reference = broker.subscribe(consumerReference, topicExpression, useNotify, precondition,
                selector, subscriptionPolicy, terminationTime);
        // END SNIPPET: subscribe
        return reference;
    }

    protected void sendNotification(ActiveMQNotificationBroker broker) {
        // START SNIPPET: notify
        String topicName = TOPIC_NAME;
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.getContent().add(topicName);

        NotificationMessageHolderType messageHolder = new NotificationMessageHolderType();
        messageHolder.setTopic(topicExpression);
        messageHolder.setMessage("Hello there! The time is: " + new Date());

        List<NotificationMessageHolderType> list = new ArrayList<NotificationMessageHolderType>(1);
        list.add(messageHolder);

        broker.notify(list);
        // END SNIPPET: notify

        assertValidMessage(list);

        System.out.println("Sending notify messages: " + list);
    }

    protected Object createMessage() {
        return "Hello there! The time is: " + new Date();
    }

    protected void assertValidMessage(List<NotificationMessageHolderType> list) {
        assertNotNull("null: list", list);

        assertTrue("Must have at least one message entry", list.size() > 0);

        int i = 0;
        for (NotificationMessageHolderType messageHolder : list) {
            i++;
            assertNotNull("null: messageHolder[" + i + "]", messageHolder);

            Object message = messageHolder.getMessage();
            assertNotNull("null: message[" + i + "]", message);
        }
    }

    protected EndpointReferenceType createReference() {
        return new EndpointReferenceType();
    }

}
