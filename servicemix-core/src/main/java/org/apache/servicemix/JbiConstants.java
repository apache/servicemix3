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
package org.apache.servicemix;

public interface JbiConstants {

    String SEND_SYNC = "javax.jbi.messaging.sendSync";
    
    String PROTOCOL_TYPE = "javax.jbi.protocol.type";
    
    String PROTOCOL_HEADERS = "javax.jbi.protocol.headers";
    
    String SECURITY_SUBJECT = "javax.jbi.security.subject";
    
    String SOAP_HEADERS = "org.apache.servicemix.soap.headers";
    
	String PERSISTENT_PROPERTY_NAME = "org.apache.servicemix.persistent";
    
    String DATESTAMP_PROPERTY_NAME = "org.apache.servicemix.datestamp";
    
    String FLOW_PROPERTY_NAME = "org.apache.servicemix.flow";
    
    String STATELESS_CONSUMER = "org.apache.servicemix.consumer.stateless";
    
    String STATELESS_PROVIDER = "org.apache.servicemix.provider.stateless";
    
}
