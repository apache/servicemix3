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

package org.servicemix.components.wsif;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.servicemix.components.util.ComponentSupport;
import org.springframework.beans.factory.InitializingBean;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.BindingOperation;

/**
 * Takes an inbound WSIF message and dispatches it into the JBI container.
 *
 * @version $Revision$
 */
public class WSIFInBinding extends ComponentSupport implements InitializingBean {
    private WSIFMarshaler marshaler = new WSIFMarshaler();
    private WSIFOperationInfo operationInfo;
    private WSIFOperation wsifOperation;
    private BindingOperation bindingOperation;

    public WSIFMarshaler getMarshaller() {
        return marshaler;
    }

    public void setMarshaller(WSIFMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public boolean process(WSIFMessage wsifMessage) throws JBIException {
        InOut exchange = getDeliveryChannel().createExchangeFactory().createInOutExchange();
        NormalizedMessage inMessage = exchange.createMessage();
        try {
            marshaler.toNMS(exchange, inMessage, operationInfo, wsifMessage);
            exchange.setInMessage(inMessage);
            if (getDeliveryChannel().sendSync(exchange)) {
                exchange.setStatus(ExchangeStatus.DONE);
                return true;
            }
            else {
                exchange.setStatus(ExchangeStatus.ERROR);
                return false;
            }
        }
        catch (WSIFException e) {
            exchange.setError(e);
            exchange.setStatus(ExchangeStatus.ERROR);
            return false;
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (operationInfo == null) {
            if (wsifOperation == null) {
                throw new IllegalArgumentException("You must specify an operationInfo or wsifOperation property");
            }
            if (bindingOperation == null) {
                throw new IllegalArgumentException("You must specify an operationInfo or bindingOperation property");
            }
            operationInfo = new WSIFOperationInfo(wsifOperation, bindingOperation);
        }
    }
}
