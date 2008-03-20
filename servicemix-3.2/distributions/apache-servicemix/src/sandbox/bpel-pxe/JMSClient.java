/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicRequestor;
import javax.jms.TopicSession;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

/**
 * @version $Revision$
 */
public class JMSClient {
    /**
     * main ...
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        ActiveMQTopic pubTopic = new ActiveMQTopic("demo.org.servicemix.source");
        System.out.println("Connecting to JMS server.");
        TopicConnection connection = factory.createTopicConnection();
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        TopicRequestor requestor = new TopicRequestor(session, pubTopic);
        System.out.println("Sending request.");
        String payload = "";
        BufferedReader reader = new BufferedReader(new FileReader("message.soap"));
        String str = null;
        while ((str = reader.readLine()) != null) {
            payload += str;
        }
        Message out = session.createTextMessage(payload);
        Message in = requestor.request(out);
        if (in == null) {
            System.out.println("Response timed out.");
        }
        else {
            System.out.println("Response was: " + in);
        }
        System.out.println("Closing.");
        connection.close();
    }
}
