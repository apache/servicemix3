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
package org.apache.servicemix.components.util;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;

/**
 * A useful base class for some kind of marshaler to and from JBI NormalizedMessage instances to
 * some kind of underlying transport or protocol.
 * 
 * @version $Revision$
 */
public class MarshalerSupport {
    private SourceTransformer transformer;

    /**
     * Converts the value to a String
     *
     * @param value the value to be coerced into a string
     * @return the value as a string or null if it cannot be converted
     */
    protected String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    // Properties
    //-------------------------------------------------------------------------
    public SourceTransformer getTransformer() {
        if (transformer == null) {
            transformer = new SourceTransformer();
        }
        return transformer;
    }

    public void setTransformer(SourceTransformer transformer) {
        this.transformer = transformer;
    }
}
