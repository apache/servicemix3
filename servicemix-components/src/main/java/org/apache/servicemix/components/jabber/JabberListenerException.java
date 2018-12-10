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
package org.apache.servicemix.components.jabber;

import org.jivesoftware.smack.packet.Packet;

/**
 * A runtime exception thrown when processing inbound Jabber packets.
 *
 * @version $Revision$
 */
public class JabberListenerException extends RuntimeException {
    private Packet packet;

    public JabberListenerException(Exception cause, Packet packet) {
        super(cause.getMessage() + ". Was processing packet: " + packet, cause);
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }
}
