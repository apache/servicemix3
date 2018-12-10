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

import java.net.URI;

import javax.xml.namespace.QName;

public interface MessageExchangeFactory
{
    MessageExchange createExchange(QName serviceName, QName operationName)
        throws MessagingException;
    
    MessageExchange createExchange(URI pattern)
        throws MessagingException;
        
    InOnly createInOnlyExchange()
        throws MessagingException;
    
    InOptionalOut createInOptionalOutExchange()
        throws MessagingException;
    
    InOut createInOutExchange()
        throws MessagingException;
    
    RobustInOnly createRobustInOnlyExchange()
        throws MessagingException;    
}
