/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.jbi.messaging;

import javax.jbi.servicedesc.ServiceEndpoint;

import javax.xml.namespace.QName;

public interface DeliveryChannel
{    
    void close()
        throws MessagingException;
    
    MessageExchangeFactory createExchangeFactory();
    
    MessageExchangeFactory createExchangeFactory(QName interfaceName);
    
    MessageExchangeFactory createExchangeFactoryForService(QName serviceName);
    
    MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint);
      
    MessageExchange accept()
        throws MessagingException;
    
    MessageExchange accept(long timeout)
        throws MessagingException;   
    
    void send(MessageExchange exchange)
        throws MessagingException;
    
    boolean sendSync(MessageExchange exchange)
        throws MessagingException;
    
    boolean sendSync(MessageExchange exchange, long timeout)
        throws MessagingException;    
}
