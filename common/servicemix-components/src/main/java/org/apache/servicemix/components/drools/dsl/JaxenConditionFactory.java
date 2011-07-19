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

import org.apache.servicemix.expression.JaxenXPathExpression;
import org.drools.rule.Rule;
import org.drools.smf.ConditionFactory;
import org.drools.smf.Configuration;
import org.drools.smf.FactoryException;
import org.drools.spi.Condition;
import org.drools.spi.RuleBaseContext;
import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;

/**
 * A condition which uses Jaxen based XPath expressions
 *
 * @version $Revision$
 */
public class JaxenConditionFactory implements ConditionFactory {

    public Condition[] newCondition(Rule rule, RuleBaseContext ruleBaseContext, Configuration configuration) throws FactoryException {
        String text = configuration.getText();
        if (text == null) {
            throw new FactoryException("No XPath provided!");
        }
        try {
            JaxenXPathExpression expression = new JaxenXPathExpression();
            expression.setXpath(text);
            expression.setNamespaceContext(createNamespaceContext(configuration));
            expression.afterPropertiesSet();
            return new Condition[]{ new JaxenCondition(rule, expression) };
        }
        catch (Exception e) {
            throw new FactoryException(e);
        }
    }

    protected NamespaceContext createNamespaceContext(Configuration configuration) {
        SimpleNamespaceContext answer = new SimpleNamespaceContext();
        String[] names = configuration.getAttributeNames();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name.equals("xmlns")) {
                answer.addNamespace("", configuration.getAttribute(name));
            }
            else {
                if (name.startsWith("xmlns:")) {
                    String prefix = name.substring(6);
                    String uri = configuration.getAttribute(name);
                    answer.addNamespace(prefix, uri);
                }
            }
        }
        return answer;
    }

}
