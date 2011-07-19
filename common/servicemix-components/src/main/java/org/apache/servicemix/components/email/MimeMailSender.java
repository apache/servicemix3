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

import org.apache.servicemix.components.util.OutBinding;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * An outbound component capable of sending MIME email via
 * <a href="http://java.sun.com/products/javamail/">JavaMail</a>
 *
 * @version $Revision$
 */
public class MimeMailSender extends OutBinding {

    private JavaMailSender sender;
    private MimeMailMarshaler marshaler = new MimeMailMarshaler();

    // Properties
    //-------------------------------------------------------------------------
    public JavaMailSender getSender() {
        return sender;
    }

    public void setSender(JavaMailSender sender) {
        this.sender = sender;
    }

    public MimeMailMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(MimeMailMarshaler marshaler) {
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
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws MessagingException {
                marshaler.prepareMessage(mimeMessage, exchange, message);
            }
        };
        sender.send(preparator);
        done(exchange);
    }

}
