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
package org.apache.servicemix.components.jabber;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * Receives inbound messages or packets and dispatches them into the NMR
 *
 * @version $Revision$
 */
public class JabberReceiver extends JabberComponentSupport {

    private PacketFilter filter;

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        if (filter == null) {
            filter = new PacketTypeFilter(Message.class);
        }
    }

    public void start() throws JBIException {
        super.start();  
        getConnection().addPacketListener(this, filter);
    }

    // Properties
    //-------------------------------------------------------------------------
    public PacketFilter getFilter() {
        return filter;
    }

    public void setFilter(PacketFilter filter) {
        this.filter = filter;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange exchange, NormalizedMessage message) throws Exception {
        throw new MessagingException("This component is not meant to receive inbound messages but received: " + message);
    }

}
