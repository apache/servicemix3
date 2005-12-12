/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.servicemix.components.mule;

import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.servicemix.components.util.OutBinding;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * Mule's lifecycle APIs confict with JBI so we must use aggregation to
 * map from JBI to a Mule component
 *
 * @version $Revision$
 */
public class JBIMessageReceiverComponent extends OutBinding {

    private JBIMessageReceiver receiver;
    private MuleMarshaler marshaler = new MuleMarshaler();

    public MuleMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(MuleMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public JBIMessageReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(JBIMessageReceiver receiver) {
        this.receiver = receiver;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
        try {
            UMOMessage umoMessage = getMarshaler().createMuleMessage(exchange, message, getBody(message));
            receiver.routeMessage(umoMessage);
            done(exchange);
        }
        catch (UMOException e) {
            throw new MuleMessagingException(e);
        }
    }

    protected void init() throws JBIException {
        super.init();

        if (getReceiver() == null) {
            throw new JBIException("You must specify the receiver property");
        }
    }


}
