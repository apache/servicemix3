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
package org.apache.servicemix.api;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.xml.namespace.QName;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class OsgiExample {

    public static class MyEndpoint implements Endpoint {

        private Channel channel;

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public void process(Exchange exchange) {
            // TODO
        }
    }


    public void testRegisteringAnEndpoint() {
        BundleContext bundleContext = null;
        
        // registering an endpoint
        Endpoint e = new MyEndpoint();
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(EndpointConstants.ID, "my-endpoint");
        props.put(EndpointConstants.SERVICE_NAME, new QName("urn:namesapce", "service"));
        props.put(EndpointConstants.ENDPOINT_NAME, "foo");
        props.put(EndpointConstants.WSDL_URL, "file:my.wsdl");

        bundleContext.registerService(Endpoint.class.getName(), e, props);
    }

    public void testFindRegistry() {
        BundleContext bundleContext = null;

        ServiceReference ref = bundleContext.getServiceReference(Registry.class.getName());
        Registry reg = (Registry) bundleContext.getService(ref);
    }

    public void testLookupReference() throws InvalidSyntaxException {
        Registry reg = null;

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(EndpointConstants.ID, "myEndpoint");
        Reference target = reg.lookup(props);
    }

    public void testSendExchange() {
        Registry reg = null;
        Reference target = null;

        Channel channel = reg.createChannel();
        try {
            Exchange e = channel.createExchange(Pattern.InOnly);
            e.setTarget(target);
            e.getIn().setContent("Hello");
            channel.send(e);
        } finally {
            channel.close();
        }
    }

}