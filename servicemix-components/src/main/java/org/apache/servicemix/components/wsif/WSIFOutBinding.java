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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.components.util.OutBinding;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;

/**
 * Consumers JBI messages and sends them as a oneway into WSIF
 *
 * @version $Revision$
 */
public class WSIFOutBinding extends OutBinding {

    private WSIFMarshaler marshaler = new WSIFMarshaler();
    private WSIFOperationMap operationMap;

    public WSIFMarshaler getMarshaller() {
        return marshaler;
    }

    public void setMarshaller(WSIFMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public WSIFOperationMap getOperationMap() {
        return operationMap;
    }

    public void setOperationMap(WSIFOperationMap operationMap) {
        this.operationMap = operationMap;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange exchange, NormalizedMessage normalizedMessage) throws Exception {
        WSIFOperationInfo operationInfo = operationMap.getOperationForExchange(exchange);
        WSIFOperation operation = operationInfo.createWsifOperation();
        WSIFMessage message = operation.createInputMessage();
        marshaler.fromNMS(operationInfo, message, normalizedMessage, getBody(normalizedMessage));
        operation.executeInputOnlyOperation(message);
        done(exchange);
    }
}
