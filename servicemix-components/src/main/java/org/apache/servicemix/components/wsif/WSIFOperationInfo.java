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
package org.apache.servicemix.components.wsif;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;

/**
 * The information about an operation.
 *
 * @version $Revision$
 */
public class WSIFOperationInfo {
    private WSIFPort port;
    private String name;

    public WSIFOperationInfo(WSIFPort port, String name) {
        this.port = port;
        this.name = name;
    }

    public WSIFOperation createWsifOperation() throws WSIFException {
        return this.port.createOperation(name);
    }
}
