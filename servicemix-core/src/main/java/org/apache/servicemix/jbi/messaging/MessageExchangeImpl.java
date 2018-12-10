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
package org.apache.servicemix.jbi.messaging;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.xml.namespace.QName;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.Set;

/**
 * A simple message exchange declaration. This is partial, just giving us enough ME function for the doodle. This
 * doesn't add anything new to the current MessageExchange definition.
 *
 * @version $Revision$
 */
public abstract class MessageExchangeImpl implements MessageExchange, Externalizable {

    public static final int SYNC_STATE_ASYNC = 0;
    public static final int SYNC_STATE_SYNC_SENT = 1;
    public static final int SYNC_STATE_SYNC_RECEIVED = 2;

    protected static final int CAN_SET_IN_MSG               = 0x00000001;
    protected static final int CAN_SET_OUT_MSG              = 0x00000002;
    protected static final int CAN_SET_FAULT_MSG            = 0x00000004;
    protected static final int CAN_PROVIDER                 = 0x00000008;
    protected static final int CAN_CONSUMER                 = 0x00000000;
    protected static final int CAN_SEND                     = 0x00000010;
    protected static final int CAN_STATUS_ACTIVE            = 0x00000040;
    protected static final int CAN_STATUS_DONE              = 0x00000080;
    protected static final int CAN_STATUS_ERROR             = 0x00000100;
    protected static final int CAN_OWNER                    = 0x00000200;

    protected static final int STATES_CANS       = 0;
    protected static final int STATES_NEXT_OUT   = 1;
    protected static final int STATES_NEXT_FAULT = 2;
    protected static final int STATES_NEXT_ERROR = 3;
    protected static final int STATES_NEXT_DONE  = 4;
    
    public static final String FAULT = "fault";
    public static final String IN = "in";
    public static final String OUT = "out";
    
    private static final long serialVersionUID = -3639175136897005605L;
    
    protected ComponentContextImpl sourceContext;
    protected ExchangePacket packet;
    protected PojoMarshaler marshaler;
    protected int state;
    protected int syncState = SYNC_STATE_ASYNC;
    protected int[][] states;
    protected MessageExchangeImpl mirror;

    /**
     * Constructor
     * @param exchangeId
     * @param pattern
     */
    public MessageExchangeImpl(String exchangeId, URI pattern, int[][] states) {
        this.states = states;
        this.packet = new ExchangePacket();
        this.packet.setExchangeId(exchangeId);
        this.packet.setPattern(pattern);
    }
    
    protected MessageExchangeImpl(ExchangePacket packet, int[][] states) {
        this.states = states;
        this.packet = packet;
    }
    
    protected MessageExchangeImpl() {
    }
    
    protected void copyFrom(MessageExchangeImpl me) {
        this.packet = me.packet;
        this.state = me.state;
        this.mirror.packet = me.packet;
        this.mirror.state = me.mirror.state;
    }
    
    protected boolean can(int c) {
        return (this.states[state][STATES_CANS] & c) == c;
    }

    /**
     * Returns the activation spec that was provided when the component was registered
     * @return the spec
     */
    public ActivationSpec getActivationSpec() {
        if (sourceContext != null) {
            return sourceContext.getActivationSpec();
        }
        return null;
    }

    /**
     * Returns the context which created the message exchange which can then be used for routing
     * @return the context
     */
    public ComponentContextImpl getSourceContext() {
        return sourceContext;
    }

    /**
     * Set the context
     * @param sourceContext
     */
    public void setSourceContext(ComponentContextImpl sourceContext) {
        this.sourceContext = sourceContext;
        this.mirror.sourceContext = sourceContext;
    }

    /**
     * @return the packet
     */
    public ExchangePacket getPacket(){
        return packet;
    }
    
    /**
     * @return URI of pattenr exchange
     */
    public URI getPattern() {
        return packet.getPattern();
    }

    /**
     * @return the exchange Id
     */
    public String getExchangeId() {
        return packet.getExchangeId();
    }

    /**
     * @return the processing status of the exchange
     */
    public ExchangeStatus getStatus() {
        if (this.packet.isAborted()) {
            return ExchangeStatus.ERROR;
        }
        return this.packet.getStatus();
    }

    /**
     * set the processing status
     *
     * @param exchangeStatus
     * @throws MessagingException
     */
    public void setStatus(ExchangeStatus exchangeStatus) throws MessagingException {
        if (!can(CAN_OWNER)) {
            throw new IllegalStateException("component is not owner");
        }
        this.packet.setStatus(exchangeStatus);

    }

    /**
     * set the source of a failure
     *
     * @param exception
     */
    public void setError(Exception exception) {
        if (!can(CAN_OWNER)) {
            throw new IllegalStateException("component is not owner");
        }
        this.packet.setError(exception);
    }

    /**
     * @return the exception describing a processing error
     */
    public Exception getError() {
        return packet.getError();
    }

    /**
     * @return the fault message for an exchange
     */
    public Fault getFault() {
        return packet.getFault();
    }

    /**
     * set the fault message for the exchange
     *
     * @param fault
     * @throws MessagingException
     */
    public void setFault(Fault fault) throws MessagingException {
        setMessage(fault, FAULT);
    }

    /**
     * @return a new message
     * @throws MessagingException
     */
    public NormalizedMessage createMessage() throws MessagingException {
        return new NormalizedMessageImpl(this);
    }

    /**
     * factory method for fault objects
     *
     * @return a new fault
     * @throws MessagingException
     */
    public Fault createFault() throws MessagingException {
        return new FaultImpl();
    }

    /**
     * get a NormalizedMessage based on the message reference
     *
     * @param name
     * @return a NormalizedMessage
     */
    public NormalizedMessage getMessage(String name) {
        if (IN.equals(name)) {
            return packet.getIn();
        } else if (OUT.equals(name)) {
            return packet.getOut();
        } else if (FAULT.equals(name)) {
            return packet.getFault();
        } else {
            return null;
        }
    }

    /**
     * set a NormalizedMessage with a named reference
     *
     * @param message
     * @param name
     * @throws MessagingException
     */
    public void setMessage(NormalizedMessage message, String name) throws MessagingException {
        if (!can(CAN_OWNER)) {
            throw new IllegalStateException("component is not owner");
        }
        if (message == null) {
            throw new IllegalArgumentException("message should not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name should not be null");
        }
        name = name.toLowerCase();
        if (IN.equals(name)) {
            if (!can(CAN_SET_IN_MSG)) {
                throw new MessagingException("In not supported");
            }
            if (packet.getIn() != null) {
                throw new MessagingException("In message is already set");
            }
            packet.setIn((NormalizedMessageImpl) message);
        } else if (OUT.equals(name)) {
            if (!can(CAN_SET_OUT_MSG)) {
                throw new MessagingException("Out not supported");
            }
            if (packet.getOut() != null) {
                throw new MessagingException("Out message is already set");
            }
            packet.setOut((NormalizedMessageImpl) message);
        } else if (FAULT.equals(name)) {
            if (!can(CAN_SET_FAULT_MSG)) {
                throw new MessagingException("Fault not supported");
            }
            if (!(message instanceof Fault)) {
                throw new MessagingException("Setting fault, but message is not a fault");
            }
            if (packet.getFault() != null) {
                throw new MessagingException("Fault message is already set");
            }
               packet.setFault((FaultImpl) message);
        } else {
            throw new MessagingException("Message name must be in, out or fault");
        }
    }

    /**
     * @param name
     * @return the proerty from the exchange
     */
    public Object getProperty(String name) {
    	if (JTA_TRANSACTION_PROPERTY_NAME.equals(name)) {
    		return packet.getTransactionContext();
    	} else if (JbiConstants.PERSISTENT_PROPERTY_NAME.equals(name)) {
    		return packet.getPersistent();
    	} else {
    		return packet.getProperty(name);
    	}
    }

    /**
     * set a named property on the exchange
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, Object value) {
        if (!can(CAN_OWNER)) {
            throw new IllegalStateException("component is not owner");
        }
        if (name == null) {
            throw new IllegalArgumentException("name should not be null");
        }
    	if (JTA_TRANSACTION_PROPERTY_NAME.equals(name)) {
    		packet.setTransactionContext((Transaction) value);
    	} else if (JbiConstants.PERSISTENT_PROPERTY_NAME.equals(name)) {
    		packet.setPersistent((Boolean) value);
    	} else {
    		packet.setProperty(name, value);
    	}
    }
    
    /**
     * @return property names
     */
    public Set getPropertyNames(){
        return packet.getPropertyNames();
    }

    /**
     * Set an endpoint
     *
     * @param endpoint
     */
    public void setEndpoint(ServiceEndpoint endpoint) {
        packet.setEndpoint(endpoint);
    }

    /**
     * set a service
     *
     * @param name
     */
    public void setService(QName name) {
        packet.setServiceName(name);
    }

    /**
     * set an operation
     *
     * @param name
     */
    public void setOperation(QName name) {
        packet.setOperationName(name);
    }
    
    /**
     * set an interface
     *
     * @param name
     */
    public void setInterfaceName(QName name) {
        packet.setInterfaceName(name);
    }

    /**
     * @return the endpoint
     */
    public ServiceEndpoint getEndpoint() {
        return packet.getEndpoint();
    }

    /**
     * @return the service
     */
    public QName getService() {
        return packet.getServiceName();
    }
    
    /**
     * @return the interface name
     */
    public QName getInterfaceName() {
        return packet.getInterfaceName();
    }

    /**
     * @return the operation
     */
    public QName getOperation() {
        return packet.getOperationName();
    }

    /**
     * @return the transaction context
     */
    public Transaction getTransactionContext() {
        return packet.getTransactionContext();
    }

    /**
     * set the transaction
     *
     * @param transaction
     * @throws MessagingException
     */
    public void setTransactionContext(Transaction transaction) throws MessagingException {
        packet.setTransactionContext(transaction);
    }

    /**
     * @return true if transacted
     */
    public boolean isTransacted() {
        return this.packet.getTransactionContext() != null;
    }

    /**
     * @return the Role of this exchange
     */
    public Role getRole() {
        return can(CAN_PROVIDER) ? Role.PROVIDER : Role.CONSUMER;
    }

    /**
     * @return the in message
     */
    public  NormalizedMessage getInMessage() {
        return this.packet.getIn();
    }

    /**
     * set the in message
     *
     * @param message
     * @throws MessagingException
     */
    public  void setInMessage(NormalizedMessage message) throws MessagingException {
        setMessage(message, IN);
    }

    /**
     * @return the out message
     */
    public  NormalizedMessage getOutMessage() {
        return getMessage(OUT);
    }

    /**
     * set the out message
     *
     * @param message
     * @throws MessagingException
     */
    public  void setOutMessage(NormalizedMessage message) throws MessagingException {
        setMessage(message, OUT);
    }
    
    /**
     * @return Returns the sourceId.
     */
    public ComponentNameSpace getSourceId() {
        return packet.getSourceId();
    }
    /**
     * @param sourceId The sourceId to set.
     */
    public void setSourceId(ComponentNameSpace sourceId) {
        packet.setSourceId(sourceId);
    }
    
    /**
     * @return Returns the destinationId.
     */
    public ComponentNameSpace getDestinationId() {
        return packet.getDestinationId();
    }
    /**
     * @param destinationId The destinationId to set.
     */
    public void setDestinationId(ComponentNameSpace destinationId) {
        packet.setDestinationId(destinationId);
    }
    
    public Boolean getPersistent() {
    	return packet.getPersistent();
    }
    
    public void setPersistent(Boolean persistent) {
    	packet.setPersistent(persistent);
    }


    public PojoMarshaler getMarshaler() {
        if (marshaler == null) {
            marshaler = new DefaultMarshaler();
        }
        return marshaler;
    }

    public void setMarshaler(PojoMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public abstract void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;        
    
    public void writeExternal(ObjectOutput out) throws IOException {
        packet.writeExternal(out);
        out.write(state);
        out.write(mirror.state);
        out.writeBoolean(can(CAN_PROVIDER));
    }
    
    public void handleSend(boolean sync) throws MessagingException {
        // Check if send / sendSync is legal
        if (!can(CAN_SEND)) {
            throw new MessagingException("illegal call to send / sendSync");
        }
        if (sync && getStatus() != ExchangeStatus.ACTIVE) {
            throw new MessagingException("illegal call to sendSync");
        }
        this.syncState = sync ? SYNC_STATE_SYNC_SENT : SYNC_STATE_ASYNC;
        // Check status
        ExchangeStatus status = getStatus();
        if (status == ExchangeStatus.ACTIVE && !can(CAN_STATUS_ACTIVE)) {
            throw new MessagingException("illegal exchange status: active");
        }
        if (status == ExchangeStatus.DONE && !can(CAN_STATUS_DONE)) {
            throw new MessagingException("illegal exchange status: done");
        }
        if (status == ExchangeStatus.ERROR && !can(CAN_STATUS_ERROR)) {
            throw new MessagingException("illegal exchange status: error");
        }
        // Check message
        // Change state
        if (status == ExchangeStatus.ACTIVE && packet.getFault() == null) {
            this.state = this.states[this.state][STATES_NEXT_OUT];
        } else if (status == ExchangeStatus.ACTIVE && packet.getFault() != null) {
            this.state = this.states[this.state][STATES_NEXT_FAULT];
        } else if (status == ExchangeStatus.ERROR) {
            this.state = this.states[this.state][STATES_NEXT_ERROR];
        } else if (status == ExchangeStatus.DONE) {
            this.state = this.states[this.state][STATES_NEXT_DONE];
        } else {
            throw new IllegalStateException("unknown status");
        }
        if (this.state < 0 || this.state >= this.states.length) {
            throw new IllegalStateException("next state is illegal");
        }
    }

    public void handleAccept() throws MessagingException {
        // Change state
        ExchangeStatus status = getStatus();
        int nextState;
        if (status == ExchangeStatus.ACTIVE && packet.getFault() == null) {
            nextState = this.states[this.state][STATES_NEXT_OUT];
        } else if (status == ExchangeStatus.ACTIVE && packet.getFault() != null) {
            nextState = this.states[this.state][STATES_NEXT_FAULT];
        } else if (status == ExchangeStatus.ERROR) {
            nextState = this.states[this.state][STATES_NEXT_ERROR];
        } else if (status == ExchangeStatus.DONE) {
            nextState = this.states[this.state][STATES_NEXT_DONE];
        } else {
            throw new IllegalStateException("unknown status");
        }
        if (nextState < 0 || nextState >= this.states.length) {
            throw new IllegalStateException("next state is illegal");
        }
        this.state = nextState;
    }

    public int getSyncState() {
        return syncState;
    }

    public MessageExchangeImpl getMirror() {
        return mirror;
    }

    public void setSyncState(int syncState) {
        this.syncState = syncState;
    }
    
}