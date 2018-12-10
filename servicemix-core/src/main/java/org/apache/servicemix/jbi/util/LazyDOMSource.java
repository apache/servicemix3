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
package org.apache.servicemix.jbi.util;

import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;

/**
 * A lazily created source which is only created if its required.
 *
 * @version $Revision$
 */
public abstract class LazyDOMSource extends DOMSource {
    private boolean initialized;

    public LazyDOMSource() {
    }

    public Node getNode() {
        if (! initialized) {
            setNode(loadNode());
            initialized = true;
        }
        return super.getNode();
    }

    protected abstract Node loadNode();
}
