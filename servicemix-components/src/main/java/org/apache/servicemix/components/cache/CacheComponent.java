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
package org.apache.servicemix.components.cache;

import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.servicemix.expression.Expression;
import org.apache.servicemix.expression.PropertyExpression;
import org.apache.servicemix.jbi.NoOutMessageAvailableException;

/**
 * Implements a caching layer on top of a service invocation to avoid calling an expensive remote service too often.
 * The cache can be a simple Map based cache or a full <a href="http://www.jcp.org/en/jsr/detail?id=107">JCache</a> instance.
 *
 * @version $Revision$
 */
public class CacheComponent extends TransformComponentSupport {

    public static final PropertyExpression KEY_PROPERTY_EXPRESSION = new PropertyExpression("org.apache.servicemix.key");

    private Map cache;
    private Expression keyExpression = KEY_PROPERTY_EXPRESSION;

    public Map getCache() {
        return cache;
    }

    public void setCache(Map cache) {
        this.cache = cache;
    }

    public Expression getKeyExpression() {
        return keyExpression;
    }

    public void setKeyExpression(Expression keyExpression) {
        this.keyExpression = keyExpression;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        super.init();
        if (cache == null) {
            throw new JBIException("You must specify a cache property");
        }
    }

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        Object key = keyExpression.evaluate(exchange, in);
        if (key != null) {
            NormalizedMessage message = (NormalizedMessage) cache.get(key);
            if (message != null) {
                getMessageTransformer().transform(exchange, message, out);
                return true;
            }
        }

        InOut inOut = getExchangeFactory().createInOutExchange();
        NormalizedMessage request = inOut.createMessage();
        getMessageTransformer().transform(exchange, in, request);
        inOut.setInMessage(request);
        getDeliveryChannel().sendSync(inOut);

        NormalizedMessage response = inOut.getOutMessage();
        Fault fault = inOut.getFault();
        Exception error = inOut.getError();
        if (fault != null) {
            fail(exchange, fault);
        }
        else if (error != null) {
            fail(exchange, error);
        }
        else if (response != null) {
            getMessageTransformer().transform(exchange, response, out);

            if (key != null) {
                cache.put(key, response);
            }
        }
        else {
            throw new NoOutMessageAvailableException(exchange);
        }
        return true;
    }

}
