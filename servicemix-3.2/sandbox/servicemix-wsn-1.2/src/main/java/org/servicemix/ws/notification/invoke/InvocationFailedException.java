/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.notification.invoke;

/**
 * Thrown if an attempted WS invocation fails
 * 
 * @version $Revision$
 */
public class InvocationFailedException extends RuntimeException {
    private Object invokeMessage;

    public InvocationFailedException(Object invokeMessage, Throwable throwable) {
        super("Failed to invoke web service. Reason: " + throwable, throwable);
        this.invokeMessage = invokeMessage;
    }

    public InvocationFailedException(Object invokeMessage, String description) {
        super(description);
        this.invokeMessage = invokeMessage;
    }

    /**
     * Returns the object for the invocation which just failed
     */
    public Object getInvokeMessage() {
        return invokeMessage;
    }
}
