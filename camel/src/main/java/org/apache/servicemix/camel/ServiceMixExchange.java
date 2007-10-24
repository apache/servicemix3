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

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.UnitOfWork;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Sep 19, 2007
 * Time: 8:50:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceMixExchange implements Exchange {

    private org.apache.servicemix.api.Exchange exchange;
    private ServiceMixMessage in;
    private ServiceMixMessage out;
    private ServiceMixMessage fault;

    public ServiceMixExchange(org.apache.servicemix.api.Exchange exchange) {
        this.exchange = exchange;
    }

    public ExchangePattern getPattern() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getProperty(String name) {
        return exchange.getProperty(name);
    }

    public <T> T getProperty(String name, Class<T> type) {
        return exchange.getProperty(name, type);
    }

    public void setProperty(String name, Object value) {
        exchange.setProperty(name, value);
    }

    public Object removeProperty(String name) {
        Object value = exchange.getProperty(name);
        exchange.setProperty(name, null);
        return value;
    }

    public Map<String, Object> getProperties() {
        return exchange.getProperties();
    }

    public Message getIn() {
        return null;
    }

    public Message getOut() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message getOut(boolean lazyCreate) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message getFault() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message getFault(boolean lazyCreate) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Throwable getException() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setException(Throwable e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isFailed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CamelContext getContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Exchange copy() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void copyFrom(Exchange source) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public UnitOfWork getUnitOfWork() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getExchangeId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setExchangeId(String id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setUnitOfWork(UnitOfWork unitOfWork) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Exchange newInstance() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
