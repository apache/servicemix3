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
package org.apache.servicemix.components.drools.dsl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.expression.JaxenVariableContext;
import org.apache.servicemix.expression.JaxenXPathExpression;
import org.drools.WorkingMemory;
import org.drools.rule.Declaration;
import org.drools.rule.Rule;
import org.drools.spi.Condition;
import org.drools.spi.Tuple;

/**
 * Represents a Jaxen based condition using the W3C DOM.
 *
 * @version $Revision$
 */
public class JaxenCondition implements Condition {

    private Rule rule;
    private JaxenXPathExpression expression;

    public JaxenCondition(Rule rule, JaxenXPathExpression expression) {
        this.rule = rule;
        this.expression = expression;
    }

    public Declaration[] getRequiredTupleMembers() {
        Collection list = rule.getParameterDeclarations();
        Declaration[] answer = new Declaration[list.size()];
        list.toArray(answer);
        return answer;
    }

    public boolean isAllowed(Tuple tuple) {
        List list = (List) rule.getParameterDeclarations();
        WorkingMemory memory = tuple.getWorkingMemory();
        JaxenVariableContext variableContext = expression.getVariableContext();
        variableContext.setVariables(null);

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Declaration declaration = (Declaration) iter.next();
            String name = declaration.getIdentifier();
            Object value = tuple.get(declaration);
            variableContext.setVariableValue(name, value);
        }
        NormalizedMessage message = (NormalizedMessage) findFirst(memory, NormalizedMessage.class);
        MessageExchange exchange = (MessageExchange) findFirst(memory, MessageExchange.class);

        try {
            return expression.matches(exchange, message);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object findFirst(WorkingMemory memory, Class type) {
        List objects = memory.getObjects(type);
        Object answer = null;
        if (objects.size() > 0) {
            answer = objects.get(0);
        }
        return answer;
    }

}
