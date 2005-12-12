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

import org.activemq.message.ActiveMQTopic;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;

import junit.framework.TestCase;

public class TopicExpressionConverterTest extends TestCase {

    private TopicExpressionConverter topicConverter = new TopicExpressionConverter();

    public void testConvert() {
        ActiveMQTopic topic1 = new ActiveMQTopic("Hello");
        TopicExpressionType type = topicConverter.toTopicExpression(topic1);
        ActiveMQTopic topic2 = topicConverter.toActiveMQTopic(type);
        assertEquals(topic1, topic2);
    }
}
