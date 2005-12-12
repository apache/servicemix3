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

import javax.jms.Topic;
import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.List;

public class TopicExpressionConverter {

    public static final String SIMPLE_DIALECT = "http://www.ibm.com/xmlns/stdwip/web-services/WSTopics/TopicExpression/simple";

    public TopicExpressionType toTopicExpression(Topic topic) {
        return toTopicExpression(topic.toString());
    }

    public TopicExpressionType toTopicExpression(ActiveMQTopic topic) {
        return toTopicExpression(topic.getPhysicalName());
    }

    public TopicExpressionType toTopicExpression(String name) {
        TopicExpressionType answer = new TopicExpressionType();
        answer.getContent().add(name);
        answer.setDialect(SIMPLE_DIALECT);
        return answer;
    }

    public ActiveMQTopic toActiveMQTopic(List<TopicExpressionType> topics) {
        if (topics == null || topics.size() == 0) {
            return null;
        }
        int size = topics.size();
        ActiveMQTopic childrenDestinations[] = new ActiveMQTopic[size];
        for (int i = 0; i < size; i++) {
            childrenDestinations[i] = toActiveMQTopic(topics.get(i));
        }

        ActiveMQTopic topic = new ActiveMQTopic();
        topic.setChildDestinations(childrenDestinations);
        return topic;
    }

    public ActiveMQTopic toActiveMQTopic(TopicExpressionType topic) {
        String dialect = topic.getDialect();
        if (dialect == null || SIMPLE_DIALECT.equals(dialect)) {
            for (Iterator iter = topic.getContent().iterator(); iter.hasNext();) {
                ActiveMQTopic answer = createActiveMQTopicFromContent(iter.next());
                if (answer != null) {
                    return answer;
                }
            }
            throw new RuntimeException("No topic name available topic: " + topic);
        }
        else {
            throw new RuntimeException("Topic dialect: " + dialect + " not supported");
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected ActiveMQTopic createActiveMQTopicFromContent(Object contentItem) {
        if (contentItem instanceof String) {
            return new ActiveMQTopic(((String) contentItem).trim());
        }
        if (contentItem instanceof QName) {
            return createActiveMQTopicFromQName((QName) contentItem);
        }
        return null;
    }

    protected ActiveMQTopic createActiveMQTopicFromQName(QName qName) {
        String localPart = qName.getLocalPart();

        // TODO we should support namespaced topics
        return new ActiveMQTopic(localPart);
    }

}
