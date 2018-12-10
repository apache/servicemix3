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
package org.apache.servicemix.components.net;

import javax.jbi.messaging.MessagingException;

/**
 * Exception thrown if the connection could not be established.
 *
 * @version $Revision$
 */
public class ConnectionRefusedException extends MessagingException {
    private int code;

    public ConnectionRefusedException(int code) {
        super("Connection refused with return code: " + code);
        this.code = code;
    }

    /**
     * @return the status code for why the connection was refused
     */
    public int getCode() {
        return code;
    }
}

