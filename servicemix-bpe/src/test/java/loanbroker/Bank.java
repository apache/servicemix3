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
package loanbroker;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.jaxp.StringSource;

public class Bank extends ComponentSupport implements MessageExchangeListener {
    
    public Bank(int number) {
        setService(new QName("urn:logicblaze:soa:bank", "Bank" + number));
        setEndpoint("bank");
    }
    
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        InOut inOut = (InOut) exchange;
        if (inOut.getStatus() == ExchangeStatus.DONE) {
            return;
        } else if (inOut.getStatus() == ExchangeStatus.ERROR) {
            return;
        }
        System.err.println(getService().getLocalPart() + " requested");
        try {
            String output = "<getLoanQuoteResponse xmlns=\"urn:logicblaze:soa:bank\"><rate>" + (Math.ceil(1000 * Math.random()) / 100) + "</rate></getLoanQuoteResponse>";
            NormalizedMessage answer = inOut.createMessage();
            answer.setContent(new StringSource(output));
            answer(inOut, answer);
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }
}