/**
 * 
 * Copyright 2005 Protique Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.components.saaj;

import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.RuntimeJBIException;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Converts a SAAJ message into a JBI message
 *
 * @version $Revision$
 */
public class SaajInBinding extends ComponentSupport {
    private SaajMarshaler marshaler = new SaajMarshaler();

    public void onSoapMessage(SOAPMessage soapMesssage) {
        try {
            InOnly messageExchange = createMessageExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();

            try {
                marshaler.toNMS(inMessage, soapMesssage);

                messageExchange.setInMessage(inMessage);
                getDeliveryChannel().send(messageExchange);
            }
            catch (SOAPException e) {
                messageExchange.setError(e);
                messageExchange.setStatus(ExchangeStatus.ERROR);
            }
        }
        catch (JBIException e) {
            throw new RuntimeJBIException(e);
        }
    }

    /**
     * Factory method to create a new inbound message exchange
     */
    protected InOnly createMessageExchange() throws JBIException {
        return getExchangeFactory().createInOnlyExchange();
    }

}
