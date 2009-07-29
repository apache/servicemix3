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
package org.apache.servicemix.samples.clients.jbi;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.RemoteServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * <p>
 * A simple JBI client to connect to a local ServiceMix instance (using remote type connection).
 * </p>
 * 
 * @author jbonofre
 */
public class Client {
    
    /**
     * <p>
     * Main method to connect to the SMX instance and send a message.
     * </p>
     * 
     * @param args main arguments.
     * @throws Exception in case of error.
     */
    public static final void main(String[] args) throws Exception {
        // get the JBI client (remote connection)
        ServiceMixClient client =  new RemoteServiceMixClient("tcp://localhost:61616");
        
        // invoking a service
        // create a in-out exchange
        InOut exchange = client.createInOutExchange();
        
        // get the "in" normalized message of the exchange
        NormalizedMessage inMessage = exchange.getInMessage();
        inMessage.setProperty("name", "smx");
        inMessage.setContent(new StringSource("<hello>world</hello>"));
        
        // define the destination endpoint
        exchange.setService(new QName("http://servicemix.apache.org/samples/wsdl-first", "PersonService"));
        
        // send the exchange 
        client.sendSync(exchange);
        
        // get the "out" normalized message
        NormalizedMessage outMessage = exchange.getOutMessage();
        
        // display the content of the "out" message
        System.out.println(outMessage.getContent().toString());
    }

}
