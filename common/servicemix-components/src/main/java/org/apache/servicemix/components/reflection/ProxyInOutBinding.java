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
package org.apache.servicemix.components.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.RuntimeJBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Proxy factory which sends the method invocations into the JBI container
 * for processing.
 *
 * @version $Revision$
 */
public class ProxyInOutBinding extends ComponentSupport implements InvocationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ProxyInOutBinding.class);

    private ClassLoader cl;
    private final Class[] interfaces;
    
    public ProxyInOutBinding(Object target) {
        this( target.getClass().getClassLoader(), target.getClass().getInterfaces());
    }
    
    public ProxyInOutBinding(ClassLoader cl, Class [] interfaces) {
        this.cl = cl;
        this.interfaces = interfaces;        
    }
    
    public Object createProxy() {
        return Proxy.newProxyInstance(cl, interfaces, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.trace("Invoked: {}", proxy);
        try {
            InOut messageExchange = getDeliveryChannel().createExchangeFactory().createInOutExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();
            inMessage.setProperty("proxy", proxy);
            inMessage.setProperty("method", method);
            inMessage.setProperty("args", args);        

            messageExchange.setInMessage(inMessage);
            if (getDeliveryChannel().sendSync(messageExchange)) {
                NormalizedMessage outMessage = messageExchange.getOutMessage();
                return getBody(outMessage);
            } else if ( messageExchange.getStatus() == ExchangeStatus.ERROR ) {
                throw messageExchange.getError();
            }
            return null;
        }
        catch (JBIException e) {
            throw new RuntimeJBIException(e);
        }
    }

}
