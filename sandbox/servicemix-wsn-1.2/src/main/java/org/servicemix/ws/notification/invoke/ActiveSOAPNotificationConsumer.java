/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.notification.invoke;

import org.codehaus.activesoap.RestClient;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.servicemix.wspojo.notification.NotificationConsumer;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;

import java.util.List;

/**
 * Invokes a remote web service using <a
 * href="http://activesoap.codehaus.org/">ActiveSOAP</a> as a Web Services
 * stack. This adapter can either invoke the endpoint as a NotificationConsumer
 * WSDL endpoint, or extract the inner messages inside the notification and
 * invoke those
 * 
 * @version $Revision$
 */
public class ActiveSOAPNotificationConsumer implements NotificationConsumer {

    private RestClient client;
    private boolean extractMessage = false;

    public ActiveSOAPNotificationConsumer(RestClient client) {
        this.client = client;
    }

    @WebMethod(operationName = "Notify")
    @Oneway
    public void notify(
            @WebParam(name = "NotificationMessage", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            List<NotificationMessageHolderType> notificationMessage) {
        if (!extractMessage) {
            for (NotificationMessageHolderType holderType : notificationMessage) {
                notifyMessage(holderType.getMessage());
            }
        }
        else {
            notifyMessage(notificationMessage);
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    /**
     * Whether or not the web service should be invoked using the entire
     * notification message or should each message inside the message holder be
     * extracted and invoked separately as a web service invocation. <p/> i.e.
     * is the endpoint invoked as a NotificationConsumer endpoint, or is it
     * invoked where the message itself is a SOAP envelope.
     */
    public boolean isExtractMessage() {
        return extractMessage;
    }

    public void setExtractMessage(boolean extractMessage) {
        this.extractMessage = extractMessage;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void notifyMessage(Object message) {
        if (message == null) {
            throw new InvocationFailedException(message, "No message available insider the message holder");
        }
        try {
            client.invokeRequestReply(message);
        }
        catch (Exception e) {
            throw new InvocationFailedException(message, e);
        }
    }

}
