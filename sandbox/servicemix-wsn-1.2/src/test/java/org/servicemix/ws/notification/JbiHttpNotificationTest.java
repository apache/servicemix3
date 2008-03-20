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

import java.io.ByteArrayOutputStream;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Notify;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Subscribe;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.servicemix.client.DefaultServiceMixClient;
import org.servicemix.jbi.container.SpringJBIContainer;
import org.servicemix.jbi.jaxp.SourceTransformer;
import org.servicemix.jbi.jaxp.StringSource;
import org.servicemix.tck.Receiver;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2003._03.addressing.ServiceNameType;

public class JbiHttpNotificationTest extends TestCase {

    SpringJBIContainer brokerContainer;
    SpringJBIContainer publisherContainer;
    SpringJBIContainer subscriberContainer;
    SourceTransformer transformer;
    
    public void setUp() throws Exception {
        brokerContainer = loadContainer("org/servicemix/ws/notification/wsn-http-broker.xml");
        publisherContainer = loadContainer("org/servicemix/ws/notification/wsn-http-publisher.xml");
        subscriberContainer = loadContainer("org/servicemix/ws/notification/wsn-http-subscriber.xml");
        transformer = new SourceTransformer();
    }
    
    public void tearDown() throws Exception {
        brokerContainer.shutDown();
        publisherContainer.shutDown();
        subscriberContainer.shutDown();
    }
    
    protected SpringJBIContainer loadContainer(String file) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(file);
        SpringJBIContainer jbi = (SpringJBIContainer) context.getBean("jbi");
        return jbi;
    }
    
    protected void sendSubscribe() throws Exception {
        // Create Subscribe request
        Subscribe subscribe = new Subscribe();
        EndpointReferenceType ep = new EndpointReferenceType();
        ServiceNameType svcName = new ServiceNameType();
        svcName.setValue(new QName("http://servicemix.org/demo", "subscriber"));
        ep.setServiceName(svcName);
        subscribe.setConsumerReference(ep);
        TopicExpressionType topic = new TopicExpressionType();
        topic.setDialect("http://www.ibm.com/xmlns/stdwip/web-services/WSTopics/TopicExpression/simple");
        topic.getContent().add("myTopic");
        subscribe.setTopicExpression(topic);
        subscribe.setUseNotify(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXBContext.newInstance(Subscribe.class).createMarshaller().marshal(subscribe, baos);
        System.err.println("Sending subscribe: " + baos.toString());
        
        // Create client (to simulate the subscriber)
        // and send the subscribe request
        DefaultServiceMixClient client = new DefaultServiceMixClient(subscriberContainer);
        InOut exchange = client.createInOutExchange();
        exchange.getInMessage().setContent(new StringSource(baos.toString()));
        QName serviceName = new QName("http://servicemix.org/demo", "broker");
        exchange.setService(serviceName);
        client.sendSync(exchange);
        if (exchange.getStatus() == ExchangeStatus.ERROR) {
            if (exchange.getError() != null) {
                throw exchange.getError();
            } else if (exchange.getFault() != null) {
                throw new Exception("FAULT: " + transformer.contentToString(exchange.getFault()));
            } else {
                throw new Exception("FAULT");
            }
        }
        System.err.println("Subscribe response: " + transformer.contentToString(exchange.getOutMessage()));
    }
    
    protected void sendNotify() throws Exception {
        // Create Notify request
        Notify notify = new Notify();
        NotificationMessageHolderType holder = new NotificationMessageHolderType();
        TopicExpressionType topic = new TopicExpressionType();
        topic.setDialect("http://www.ibm.com/xmlns/stdwip/web-services/WSTopics/TopicExpression/simple");
        topic.getContent().add("myTopic");
        holder.setTopic(topic);
        holder.setMessage("my message");
        notify.getNotificationMessage().add(holder);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXBContext.newInstance(Subscribe.class).createMarshaller().marshal(notify, baos);
        System.err.println("Sending notify: " + baos.toString());
        
        // Send Notify request
        DefaultServiceMixClient client = new DefaultServiceMixClient(publisherContainer);
        RobustInOnly exchange = client.createRobustInOnlyExchange();
        exchange.getInMessage().setContent(new StringSource(baos.toString()));
        QName serviceName = new QName("http://servicemix.org/demo", "broker");
        exchange.setService(serviceName);
        client.sendSync(exchange);
        if (exchange.getStatus() == ExchangeStatus.ERROR) {
            if (exchange.getError() != null) {
                throw exchange.getError();
            } else if (exchange.getFault() != null) {
                throw new Exception("FAULT: " + transformer.contentToString(exchange.getFault()));
            } else {
                throw new Exception("FAULT");
            }
        }
        System.err.println("Notify send successfully !");
    }
    
    public void test() throws Exception {
        sendSubscribe();
        sendNotify();
        
        Receiver receiver = (Receiver) subscriberContainer.getBean("receiver");
        receiver.getMessageList().assertMessagesReceived(1);
    }
}
