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
package org.apache.servicemix.components.util;

import java.io.OutputStream;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

/**
 * A Component that dumps a message to a stream
 * 
 * @version $Revision$
 */
public class StreamWriterComponent extends OutBinding {

    private OutputStream out = System.out;

    /**
     * @return Returns the out.
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * @param out The out to set.
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    

    // Implementation methods
    // -------------------------------------------------------------------------
    

    protected void process(MessageExchange exchange, NormalizedMessage message) throws Exception {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(message.getContent(), new StreamResult(out));
        done(exchange);
    }
}
