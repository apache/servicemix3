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

import java.util.Date;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;


/**
 * @version $Revision$
 */
public class JMSClient {
    
    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        ActiveMQTopic pubTopic = new ActiveMQTopic("demo.org.servicemix.source");
        ActiveMQTopic subTopic = new ActiveMQTopic("demo.org.servicemix.result");
        
        System.out.println("Connecting to JMS server.");
        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(pubTopic);
        MessageConsumer consumer = session.createConsumer(subTopic);
        connection.start();
        
        System.out.println("Sending request.");
        producer.send(session.createTextMessage("<timestamp>"+new Date()+"</timestamp>"));
        TextMessage m = (TextMessage) consumer.receive(1000*10);
        if( m == null ) {
            System.out.println("Response timed out.");
        } else {
            System.out.println("Response was: "+m.getText());
        }
        
        System.out.println("Closing.");
        connection.close();
    }
}
