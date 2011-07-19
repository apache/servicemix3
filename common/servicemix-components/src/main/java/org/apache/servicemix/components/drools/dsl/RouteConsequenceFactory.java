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

import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

import org.apache.servicemix.components.drools.JbiHelper;
import org.drools.rule.Rule;
import org.drools.smf.Configuration;
import org.drools.smf.ConsequenceFactory;
import org.drools.smf.FactoryException;
import org.drools.spi.Consequence;
import org.drools.spi.RuleBaseContext;
import org.drools.spi.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision$
 */
public class RouteConsequenceFactory implements ConsequenceFactory {

    private static transient Logger logger = LoggerFactory.getLogger(RouteConsequenceFactory.class);

    public Consequence newConsequence(Rule rule, RuleBaseContext ruleBaseContext, Configuration configuration) throws FactoryException {
        final QName operation = toQName(configuration, configuration.getAttribute("operation"));
        final QName service = toQName(configuration, configuration.getAttribute("service"));
        final QName interfaceName = toQName(configuration, configuration.getAttribute("interface"));
        return new JbiConsequence() {
            protected void invokeJbiOperation(JbiHelper helper, Tuple tuple) throws MessagingException {
                helper.route(service, operation, interfaceName);
            }
        };
    }

    /**
     * Converts the String into a QName
     */
    protected QName toQName(Configuration configuration, String text) {
        if (text == null) {
            return null;
        }
        String[] names = configuration.getAttributeNames();
        String localPart = text;
        String prefix = null;
        int idx = text.indexOf(':');
        if (idx >= 0) {
            prefix = "xmlns:" + text.substring(0, idx);
            localPart = text.substring(idx + 1);
        }
        String uri = "";
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (prefix == null) {
                if ("xmlns".equals(name)) {
                    uri = configuration.getAttribute(name);
                    break;
                }
            }
            else {
                if (name.equals(prefix)) {
                    uri = configuration.getAttribute(name);
                    break;
                }
            }
        }
        logger.debug("Creating QName with");
        logger.debug("uri: {}", uri);
        logger.debug("name: {}", localPart);
        return new QName(uri, localPart);
    }

}
