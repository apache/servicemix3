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
package org.apache.servicemix.api;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents a message exchange.
 *
 * An exchange is used to interact with a channel
 * representing a link to a logical endpoint.
 * Exchanges are created using the {@link Channel}.
 *
 * @version $Revision: $
 * @since 4.0
 */
public interface Exchange extends Serializable, Cloneable {

    enum Pattern {
        InOnly,
        RobustInOnly,
        InOut,
        InOptionalOut;

        public boolean has(Message.Type msg) {
            switch (msg) {
                case In:
                    return true;
                case Out:
                    return this == InOut || this == InOptionalOut;
                case Fault:
                    return this != InOnly;
                default:
                    return false;
            }
        }
    }

    enum Role {
        Consumer,
        Provider,
    }

    /**
     * The role of the exchange.
     * @return
     */
    Role getRole();

    /**
     * The exchange pattern
     * @return
     */
    Exchange.Pattern getPattern();

    Reference getTarget();

    void setTarget(Reference target);

    /**
     *
     * @return the names of properties set on this exchange
     */
    Set<String> getPropertyNames();

    /**
     * Get a given property by its name.
     *
     * @param name the name of the property to retrieve
     * @return the value of the property or <code>null</code> if none has been set
     */
    Object      getProperty(String name);

    /**
     * Set a property on this exchange.
     * Giving <code>null</code> will actually remove the property for the list.
     *
     * @param name the name of the property
     * @param value the value for this property or <code>null</code>
     */
    void        setProperty(String name, Object value);

    /**
     * Obtains the input message
     * @return the input message or <code>null</code> if
     *         this pattern do not have any
     */
    Message getIn();

    /**
     * Obtains the output message
     * @return the output message or <code>null</code> if
     *         this pattern does not have any
     */
    Message getOut();

    /**
     * Obtains the fault message
     * @return the fault message or <code>null</code> if
     *         this pattern does not have any
     */
    Message getFault();

    /**
     * Obtains the message of the given type
     * @return the message or <code>null</code> if
     *         this pattern does not support this type of message
     */
    Message getMessage(Message.Type dir);

    /**
     * Obtains the error of this exchange
     * @return the exception that caused the exchange to fail
     */
    Exception getError();

    /**
     * Make sure that all streams contained in the content and in
     * attachments are transformed to re-readable sources.
     * This method will be called by the framework when persisting
     * the exchange or when displaying it
     */
    void        ensureReReadable();

    void     copyFrom(Exchange exchange);
    Exchange copy();
    String   display(boolean displayContent);

}
