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
package org.apache.servicemix.jbi;

import javax.jbi.JBIException;

/**
 * An exception thrown if a component cannot be properly initialised due to a missing piece of configuration.
 *
 * @version $Revision$
 */
public class MissingPropertyException extends JBIException {
    private String property;

    public MissingPropertyException(String property) {
        super("Cannot use this component as the property '" + property + "' was not configured");
        this.property = property;
    }

    /**
     * Returns the name of the missing property
     */
    public String getProperty() {
        return property;
    }
}
