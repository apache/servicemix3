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
package org.apache.servicemix.components.groovy;

import groovy.xml.DOMBuilder;

import org.apache.servicemix.components.script.ScriptComponent;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.script.Bindings;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A component which is capable of invoking a <a href="http://groovy.codehaus.org/">Groovy</a> script to process
 * or transform a message.
 *
 * @version $Revision$
 */
public class GroovyComponent extends ScriptComponent {

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;

    public GroovyComponent() {
        setScriptEngineName("groovy");
    }

    public GroovyComponent(QName service, String endpoint) {
        super(service, endpoint);
        setScriptEngineName("groovy");
    }

    public DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (documentBuilder == null) {
            documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
        }
        return documentBuilder;
    }

    public void setDocumentBuilder(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    public DocumentBuilderFactory getDocumentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        }
        return documentBuilderFactory;
    }

    public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
        this.documentBuilderFactory = documentBuilderFactory;
    }

    protected void populateBindings(Bindings bindings, MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        try {
            super.populateBindings(bindings, exchange, in, out);

            // lets output a builder
            DocumentBuilder documentBuilder = getDocumentBuilder();
            bindings.put("builder", new DOMBuilder(documentBuilder));
        }
        catch (ParserConfigurationException e) {
            throw new MessagingException("Failed to create DOM DocumentBuilder: " + e, e);
        }
    }

}
