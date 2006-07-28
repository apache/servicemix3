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
package org.apache.servicemix.components.mule;

import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOException;

/**
 * A receiver of JBI events which are dispatched into Mule
 *
 * @version $Revision$
 */
public class JBIMessageReceiver extends AbstractMessageReceiver {
    private String connectionDescription = "JBIMessageReceiver";
    private boolean connected;


    // Mule methods
    //-------------------------------------------------------------------------
    public void start() throws UMOException {
    }

    public void stop() throws UMOException {
    }

    public void connect() throws Exception {
        connected = true;
    }

    public void disconnect() throws Exception {
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getConnectionDescription() {
        return connectionDescription;
    }

    public void setConnectionDescription(String connectionDescription) {
        this.connectionDescription = connectionDescription;
    }


}
