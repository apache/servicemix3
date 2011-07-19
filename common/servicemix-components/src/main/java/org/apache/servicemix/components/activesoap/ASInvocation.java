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
package org.apache.servicemix.components.activesoap;

import org.codehaus.activesoap.transport.Invocation;
import org.codehaus.activesoap.util.XMLStreamFactory;

import javax.xml.stream.XMLStreamReader;

/**
 * An <a href="http://activesoap.codehaus.org/">ActiveSOAP</a> {@link Invocation} which uses JBI.
 *
 * @version $Revision$
 */
public class ASInvocation extends Invocation {

    private ASTransport asTransport;

    public ASInvocation(ASTransport transport, XMLStreamFactory streamFactory) {
        super(transport, streamFactory);
        this.asTransport = transport;
    }

    public void invokeOneWay() throws Exception {
        String xml = getRequestText();
        asTransport.invokeOneWay(this, xml);
    }

    public XMLStreamReader invokeRequest() throws Exception {
        String xml = getRequestText();
        return asTransport.invokeRequest(this, xml);
    }

}
