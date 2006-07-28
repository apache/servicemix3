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
package org.servicemix.ws.notification;

import EDU.oswego.cs.dl.util.concurrent.Slot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.servicemix.wspojo.notification.NotificationConsumer;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.RequestWrapper;

import java.util.List;

/**
 * A simple NotificationConsumer used for unit testing.
 * 
 * @version $Revision$
 */
public class StubNotificationConsumer implements NotificationConsumer {
    private static final transient Log log = LogFactory.getLog(StubNotificationConsumer.class);

    private Slot result;
    private int timeout;

    public StubNotificationConsumer() {
        this(new Slot());
    }

    public StubNotificationConsumer(Slot result) {
        this.result = result;
    }

    @WebMethod(operationName = "Notify")
    @Oneway
    @RequestWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Notify", localName = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    public void notify(
            @WebParam(name = "NotificationMessage", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            List<NotificationMessageHolderType> list) {

        System.out.println("#### Received inbound notification: " + list);
        try {
            timeout = 5000;
            result.offer(list, timeout);
        }
        catch (InterruptedException e) {
            log.warn("Could not offer notification: " + e, e);
        }
    }

    public Slot getResult() {
        return result;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
