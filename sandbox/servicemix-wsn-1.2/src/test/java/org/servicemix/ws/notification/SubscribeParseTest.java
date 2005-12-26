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

package org.servicemix.ws.notification;

import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Subscribe;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class SubscribeParseTest extends TestCase {

    protected JAXBContext context;

    public void testParseXmlBeansUsingURL() throws Exception {
        Subscribe doc = (Subscribe) context.createUnmarshaller().unmarshal(getClass().getResource("wsn-subscribe.xml"));

        System.out.println("Parsed: " + doc);
        System.out.println("Service: " + doc.getConsumerReference().getServiceName().getValue());
    }

    protected void setUp() throws Exception {
        super.setUp();

        context = createContext();
    }

    protected JAXBContext createContext() throws Exception {
        return JAXBContext.newInstance(Subscribe.class, EndpointReferenceType.class);
    }
}
