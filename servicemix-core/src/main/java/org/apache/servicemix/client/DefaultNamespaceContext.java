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

import javax.xml.xpath.*;
import javax.xml.namespace.NamespaceContext;

import java.util.*;

/**
 * An implementation of {@link NamespaceContext} which uses a simple Map where
 * the keys are the prefixes and the values are the URIs
 * 
 * @version $Revision: $
 */
public class DefaultNamespaceContext implements NamespaceContext {

    private final Map map;
    private final NamespaceContext parent;

    public DefaultNamespaceContext() {
        this.map = new HashMap();
        XPathFactory factory = XPathFactory.newInstance();
        this.parent = factory.newXPath().getNamespaceContext();
    }

    public DefaultNamespaceContext(NamespaceContext parent, Map map) {
        this.parent = parent;
        this.map = map;
    }

    /**
     * A helper method to make it easy to create newly populated instances
     */
    public DefaultNamespaceContext add(String prefix, String uri) {
        map.put(prefix, uri);
        return this;
    }
    
    public String getNamespaceURI(String prefix) {
        String answer = (String) map.get(prefix);
        if (answer == null && parent != null) {
            return parent.getNamespaceURI(prefix);
        }
        return answer;
    }

    public String getPrefix(String namespaceURI) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (namespaceURI.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        if (parent != null) {
            return parent.getPrefix(namespaceURI);
        }
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        Set set = new HashSet();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (namespaceURI.equals(entry.getValue())) {
                set.add(entry.getKey());
            }
        }
        if (parent != null) {
            Iterator iter = parent.getPrefixes(namespaceURI);
            while (iter.hasNext()) {
                set.add(iter.next());
            }
        }
        return set.iterator();
    }
}
