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
package org.apache.servicemix.camel;

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.servicemix.nmr.api.Channel;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Status;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Sep 19, 2007
 * Time: 8:59:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceMixConsumer extends DefaultConsumer implements org.apache.servicemix.nmr.api.Endpoint {

    private Channel channel;

    public ServiceMixConsumer(ServiceMixEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    public ServiceMixEndpoint getEndpoint() {
        return (ServiceMixEndpoint) super.getEndpoint();
    }

    protected void doStart() throws Exception {
        super.doStart();
        getEndpoint().getComponent().registerEndpoint(this, null);
    }

    protected void doStop() throws Exception {
        getEndpoint().getComponent().unregisterEndpoint(this, null);
        super.doStop();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void process(Exchange exchange) {
        if (exchange.getStatus() == Status.Active) {
            try {
                getAsyncProcessor().process(new ServiceMixExchange(exchange));
            } catch (Exception e) {
                exchange.setError(e);
                exchange.setStatus(Status.Error);
                channel.send(exchange);
            }
        }
    }
}
