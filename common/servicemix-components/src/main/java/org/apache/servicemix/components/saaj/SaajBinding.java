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
package org.apache.servicemix.components.saaj;

import java.io.ByteArrayOutputStream;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.TransformComponentSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts an inbound JBI message into a <a href="http://java.sun.com/xml/saaj/">SAAJ</a> (Soap With Attachments for Java)
 * request-response and outputs the response back into JBI Bindings. This provides
 * a message centric way of invoking SOAP services inside providers such as <a href="http://ws.apache.org/axis/">Apache Axis</a>
 *
 * @version $Revision$
 */
public class SaajBinding extends TransformComponentSupport implements MessageExchangeListener {

    private static final transient Logger logger = LoggerFactory.getLogger(SaajBinding.class);

    private SaajMarshaler marshaler = new SaajMarshaler();
    private SOAPConnectionFactory connectionFactory;
    private Object soapEndpoint;
    private String soapAction;


    public SOAPConnectionFactory getConnectionFactory() throws SOAPException {
        if (connectionFactory == null) {
            connectionFactory = createConnectionFactory();
        }
        return connectionFactory;
    }

    public void setConnectionFactory(SOAPConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Object getSoapEndpoint() {
        return soapEndpoint;
    }

    public void setSoapEndpoint(Object soapEndpoint) {
        this.soapEndpoint = soapEndpoint;
    }

    /**
     * @deprecated use getMarshaler instead
     */
    public SaajMarshaler getMarshaller() {
        return marshaler;
    }

    /**
     * @deprecated use setMarshaler instead
     */
    public void setMarshaller(SaajMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return the marshaler
     */
    public SaajMarshaler getMarshaler() {
        return marshaler;
    }

    /**
     * @param marshaler the marshaler to set
     */
    public void setMarshaler(SaajMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        SOAPConnection connection = getConnectionFactory().createConnection();
        try {
            SOAPMessage inMessage = marshaler.createSOAPMessage(in);
            MimeHeaders mh = inMessage.getMimeHeaders();
            if (mh.getHeader("SOAPAction") == null) {
                if (soapAction != null && soapAction.length() > 0) {
                    mh.addHeader("SOAPAction", soapAction);
                } else {
                    mh.addHeader("SOAPAction", "\"\"");
                }
                inMessage.saveChanges();
            }

            if (logger.isDebugEnabled()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                inMessage.writeTo(buffer);
                logger.debug(new String(buffer.toByteArray()));
            }
            
            SOAPMessage response = connection.call(inMessage, soapEndpoint);
            if (response != null) {
                marshaler.toNMS(out, response);
                return true;
            } else {
                return false;
            }
        }
        finally {
            try {
                connection.close();
            }
            catch (SOAPException e) {
                logger.warn("Failed to close connection", e);
            }
        }
    }

    protected SOAPConnectionFactory createConnectionFactory() throws SOAPException {
        return SOAPConnectionFactory.newInstance();
    }


    protected SOAPConnection createConnection() throws SOAPException {
        return getConnectionFactory().createConnection();
    }

    /**
	 * @return Returns the soapAction.
	 */
	public String getSoapAction() {
		return soapAction;
	}
 
	/**
	 * @param soapAction
	 *            The soapAction to set.
	 */
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

}
