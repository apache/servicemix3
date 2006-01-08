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
package org.apache.servicemix.components.mule;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.lifecycle.InitialisationException;


/**
 * <code>ServiceMixConnector</code> connector to ServiceMix
 *
 * @version $Revision$
 */
public class JBIConnector extends AbstractServiceEnabledConnector {

    /**
     * @return the protocol name
     */
    public String getProtocol() {
        return "servicemix";
    }

    public void doInitialise() throws InitialisationException {
        super.doInitialise();
    }

}