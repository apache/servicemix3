/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2005 Exist Software Engineering. All rights reserved.
 */

package org.servicemix.components.mule;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * ServiceMixMessageDispatcherFactory the UMOMessageDispatcherFactory implementation
 * <p/>
 * * @version $Revision$
 */


public class JBIMessageDispatcherFactory implements UMOMessageDispatcherFactory {

    /**
     * @param connector
     * @return the message dispatcher
     * @throws UMOException
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
        return new JBIMessageDispatcher((JBIConnector) connector);
    }

}