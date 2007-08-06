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

/**
 * Represents an endpoint to expose in the NMR.
 * Exposing an endpoint in the NMR is done using the (@link Registry}. 
 *
 * The endpoint will be given Exchange to process and must be prepared to
 * be given several exchanges concurrently for processing.
 *
 * @version $Revision: $
 * @since 4.0
 */
public interface Endpoint {

    /**
     * Set the channel so that the endpoint can send exchanges back
     * when they are processed or act as a consumer itself.
     * This method will be called by the NMR while the endpoint is registered.
     *
     * @param channel the channel that this endpoint can use
     */
    void setChannel(Channel channel);

    /**
     * Process the given exchange.  The processing can occur in the current thread
     * or asynchronously.
     * If an exchange has sent an exchange asynchronously to another endpoint,
     * it will receive the exchange back using this method.  An endpoint can
     * recognized such exchanges by checking the role of the exchange.
     *
     * @param exchange the exchange to process
     */
    void process(Exchange exchange);

}
