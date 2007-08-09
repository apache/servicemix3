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
package org.apache.servicemix.components.util;

import java.util.Iterator;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.jbi.jaxp.ResourceSource;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.springframework.core.io.Resource;

/**
 * A simple mock service component which is hard coded with a response to give
 * which can be very useful for mocking out a web service call with some static
 * response. For more complex requirements consider using a Script component or
 * maybe a Jelly based component etc.
 * 
 * @version $Revision$
 */
public class MockServiceComponent extends TransformComponentSupport implements MessageExchangeListener {
    
    private Source responseContent;
    private String responseXml;
    private Resource responseResource;
    private Map responseProperties;

    public MockServiceComponent() {
    }
    
    public MockServiceComponent(QName service, String endpoint) {
        super(service, endpoint);
    }
    
    public Source getResponseContent() {
        if (responseContent == null) {
            if (responseXml != null) {
                responseContent = new StringSource(responseXml);
            } else if (responseResource != null) {
                return new ResourceSource(responseResource);
            }
        }
        return responseContent;
    }

    public void setResponseContent(Source responseContent) {
        this.responseContent = responseContent;
    }

    public Map getResponseProperties() {
        return responseProperties;
    }

    public void setResponseProperties(Map responseProperties) {
        this.responseProperties = responseProperties;
    }

    public String getResponseXml() {
        return responseXml;
    }

    public void setResponseXml(String responseXml) {
        this.responseXml = responseXml;
    }

    public Resource getResponseResource() {
        return responseResource;
    }

    public void setResponseResource(Resource responseResource) {
        this.responseResource = responseResource;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void init() throws JBIException {
        super.init();
        if (getResponseContent() == null) {
            throw new IllegalArgumentException("You must specify the 'responseContent', 'responseXml' or 'responseResource' properties");
        }
    }

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        getMessageTransformer().transform(exchange, in, out);
        out.setContent(getResponseContent());
        Map map = getResponseProperties();
        if (map != null) {
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                out.setProperty(name, value);
            }
        }
        return true;
    }
}
