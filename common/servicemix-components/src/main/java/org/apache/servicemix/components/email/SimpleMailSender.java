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
package org.apache.servicemix.components.email;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.components.util.OutBinding;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * An outbound component capable of sending simple email using Spring
 *
 * @version $Revision$
 */
public class SimpleMailSender extends OutBinding {

    private MailSender sender;
    private SimpleMailMarshaler marshaler = new SimpleMailMarshaler();

    // Properties
    //-------------------------------------------------------------------------
    public MailSender getSender() {
        return sender;
    }

    public void setSender(MailSender sender) {
        this.sender = sender;
    }

    public SimpleMailMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(SimpleMailMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        super.init();
        if (sender == null) {
            throw new JBIException("You must configure the sender property");
        }
    }

    protected void process(final MessageExchange exchange, final NormalizedMessage message) throws Exception {
        SimpleMailMessage email = new SimpleMailMessage();
        marshaler.prepareMessage(email, exchange, message);
        sender.send(email);
        done(exchange);
    }

}
