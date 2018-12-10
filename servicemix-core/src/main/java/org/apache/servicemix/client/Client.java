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
package org.apache.servicemix.client;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessagingException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides a way to look up a {@link Destination} via various means such as URIs
 * so that you can send and receive messages using a simple POJO based API.
 * 
 * @version $Revision: $
 */
public interface Client {

    /**
     * Creates the endpoint for the given endpoint URI.
     * 
     * The endpoint URI describes the underlying JBI component
     * using a simple URI syntax. 
     * 
     * You can access JBI endpoints using URI syntax
     * 
     * <ul>
     * <li>interface:http:/foo.com/whatever/localName?operation=http://foo.com</li>
     * <li>service:http:/foo.com/whatever/localName?endpoint=cheese</li>
     * </ul>
     * 
     * Or you can access underlying transports using transport specific URIs
     * 
     * <ul>
     * <li>jms://provider/queue/FOO.BAR</li>
     * <li>file:/path</li>
     * </ul>
     * 
     * @param uri
     * @return the endpoint for the given uri
     * @throws JBIException 
     */
    public Destination createEndpoint(URI uri) throws JBIException;
    
    /**
     * Creates the endpoint for the given endpoint URI.
     * 
     * @see #createEndpoint(URI)
     * 
     * @param uri
     * @return the endpoint for the given uri
     * @throws URISyntaxException if the string is not a valid URI syntax
     */
    public Destination createEndpoint(String uri) throws JBIException, URISyntaxException;
}
