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

import java.util.concurrent.Future;

/**
 * Creates a channel to perform invocations on the NMR.
 *
 * @version $Revision: $
 * @since 4.0
 */
public interface Channel {

    /**
     * Used for asynchronous notification of the exchange processing being complete
     *
     * @version $Revision: $
     */
    public interface AsyncHandler {

        void handle(Exchange exchange);

    }

    /**
     * Creates a new exchange.
     *
     * @param pattern specify the InOnly / InOut / RobustInOnly / RobustInOut
     * @return a new exchange of the given pattern
     */
    Exchange createExchange(Exchange.Pattern pattern);

    /**
     * Synchronously invocation of the service
     */
    void invoke(Exchange exchange);

    /**
     * An asynchronous invocation of the service which will notify the returned future when the invocation
     * is complete
     *
     * @param exchange
     * @return a future to be invoked with the exchange when it is complete
     */
    Future<Exchange> invokeAsync(Exchange exchange);

    /**
     * An asynchronous invocation of the service which will invoke the handler when the invocation
     * is completed
     *
     * @param exchange the exchange to invoke
     * @param handler the handler invoked, typically from another thread, when the invocation is completed
     * to avoid a thread context switch
     *
     * @return a future so that the invocation can be canceled
     */
    Future<Exchange> invokeAsync(Exchange exchange, AsyncHandler handler);

    /**
     * Closes the channel, freeing up any resources (like sockets, threads etc)
     */
    void close();

}

