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
package org.apache.servicemix.jbi.runtime.impl;

import org.apache.servicemix.api.Exchange;
import org.apache.servicemix.api.Message;
import org.apache.servicemix.api.Pattern;
import org.apache.servicemix.api.Status;
import org.apache.servicemix.core.MessageImpl;

import javax.jbi.messaging.*;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 5, 2007
 * Time: 2:21:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageExchangeImpl implements MessageExchange  {

    private final Exchange exchange;

    public MessageExchangeImpl(Exchange exchange) {
        this.exchange = exchange;
    }

    public Exchange getInternalExchange() {
            return exchange;
    }

    public String getExchangeId() {
        return exchange.getId();
    }

    public URI getPattern() {
        return URI.create(exchange.getPattern().getWsdlUri());
    }

    public MessageExchange.Role getRole() {
        return exchange.getRole() == org.apache.servicemix.api.Role.Consumer ? MessageExchange.Role.CONSUMER : MessageExchange.Role.PROVIDER;
    }

    public ExchangeStatus getStatus() {
        if (exchange.getStatus() == Status.Active) {
            return ExchangeStatus.ACTIVE;
        } else if (exchange.getStatus() == Status.Done) {
            return ExchangeStatus.DONE;
        } else if (exchange.getStatus() == Status.Error) {
            return ExchangeStatus.ERROR;
        } else {
            throw new IllegalStateException();
        }
    }

    public void setStatus(ExchangeStatus status) {
        if (status == ExchangeStatus.ACTIVE) {
            exchange.setStatus(Status.Active);
        } else if (status == ExchangeStatus.DONE) {
            exchange.setStatus(Status.Done);
        } else if (status == ExchangeStatus.ERROR) {
            exchange.setStatus(Status.Error);
        } else {
            throw new IllegalStateException();
        }

    }

    public QName getOperation() {
        return exchange.getOperation();
    }

    public void setOperation(QName qName) {
        exchange.setOperation(qName);
    }

    public Object getProperty(String s) {
        return exchange.getProperty(s);
    }

    public void setProperty(String s, Object o) {
        exchange.setProperty(s, o);
    }

    public Set getPropertyNames() {
        return exchange.getProperties().keySet();
    }

    public NormalizedMessage createMessage() throws MessagingException {
        return new NormalizedMessageImpl(new MessageImpl());
    }

    public NormalizedMessage getMessage(String name) {
        if ("in".equalsIgnoreCase(name)) {
            return getInMessage();
        } else if ("out".equalsIgnoreCase(name)) {
            return getOutMessage();
        } else if ("fault".equalsIgnoreCase(name)) {
            return getFault();
        } else {
            throw new IllegalStateException();
        }
    }

    public void setMessage(NormalizedMessage msg, String name) throws MessagingException {
        if ("in".equalsIgnoreCase(name)) {
            setInMessage(msg);
        } else if ("out".equalsIgnoreCase(name)) {
            setOutMessage(msg);
        } else if ("fault".equalsIgnoreCase(name)) {
            setFault((Fault) msg);
        } else {
            throw new IllegalStateException();
        }
    }

    public NormalizedMessage getInMessage() {
        Message msg = exchange.getIn(false);
        if (msg == null) {
            return null;
        } else {
            return new NormalizedMessageImpl(msg);
        }
    }

    public void setInMessage(NormalizedMessage message) throws MessagingException {
        NormalizedMessageImpl msg = (NormalizedMessageImpl) message;
        exchange.setIn(msg.getInternalMessage());
    }

    public NormalizedMessage getOutMessage() {
        Message msg = exchange.getOut(false);
        if (msg == null) {
            return null;
        } else {
            return new NormalizedMessageImpl(msg);
        }
    }

    public void setOutMessage(NormalizedMessage message) throws MessagingException {
        NormalizedMessageImpl msg = (NormalizedMessageImpl) message;
        exchange.setOut(msg.getInternalMessage());
    }

    public Fault createFault() throws MessagingException {
        return new FaultImpl(new MessageImpl());
    }

    public Fault getFault() {
        Message msg = exchange.getFault(false);
        if (msg == null) {
            return null;
        } else {
            return new FaultImpl(msg);
        }
    }

    public void setFault(Fault message) throws MessagingException {
        FaultImpl msg = (FaultImpl) message;
        exchange.setFault(msg.getInternalMessage());
    }

    public Exception getError() {
        return exchange.getError();
    }

    public void setError(Exception e) {
        exchange.setError(e);
    }

    public void setEndpoint(ServiceEndpoint endpoint) {
        exchange.setProperty("javax.jbi.ServiceEndpoint", endpoint);
    }

    public void setService(QName service) {
        exchange.setProperty("javax.jbi.ServiceeName", service);
    }

    public void setInterfaceName(QName interfaceName) {
        exchange.setProperty("javax.jbi.InterfaceName", interfaceName);
    }

    public ServiceEndpoint getEndpoint() {
        return exchange.getProperty("javax.jbi.ServiceEndpoint", ServiceEndpoint.class);
    }

    public QName getInterfaceName() {
        return exchange.getProperty("javax.jbi.InterfaceName", QName.class);
    }

    public QName getService() {
        return exchange.getProperty("javax.jbi.ServiceName", QName.class);
    }

    public boolean isTransacted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
