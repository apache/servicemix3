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
package org.apache.servicemix.components.saaj;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;

/**
 * Converts an inbound JBI message into a <a href="http://java.sun.com/xml/saaj/">SAAJ</a> (Soap With Attachments for Java)
 * request-response and outputs the response back into JBI Bindings. This provides
 * a message centric way of invoking SOAP services inside providers such as <a href="http://ws.apache.org/axis/">Apache Axis</a>
 *
 * @version $Revision$
 */
public class SaajBinding extends ComponentSupport implements MessageExchangeListener {

    private static final transient Log log = LogFactory.getLog(SaajBinding.class);

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

    public SaajMarshaler getMarshaller() {
        return marshaler;
    }

    public void setMarshaller(SaajMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            return;
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            return;
        }
        SOAPConnection connection = null;
        try {
            connection = getConnectionFactory().createConnection();

            SOAPMessage inMessage = marshaler.createSOAPMessage(exchange.getMessage("in"));
            if (soapAction != null) {
				MimeHeaders mh = inMessage.getMimeHeaders();
				if (mh.getHeader("SOAPAction") == null) {
					mh.addHeader("SOAPAction", "\"" + soapAction + "\"");
					inMessage.saveChanges();
				}
			}
            
            SOAPMessage response = connection.call(inMessage, soapEndpoint);

            NormalizedMessage outMessage = exchange.createMessage();
            marshaler.toNMS(outMessage, response);

            answer(exchange, outMessage);
        }
        catch (Exception e) {
            fail(exchange, e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SOAPException e) {
                    log.warn("Failed to close connection: " + e, e);
                }
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
