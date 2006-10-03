package org.apache.servicemix.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.RobustInOnly;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jbi.JBIException;
import javax.xml.namespace.QName;

public abstract class ProviderEndpoint extends Endpoint implements ExchangeProcessor {

    private ServiceEndpoint activated;
    private DeliveryChannel channel;
    private MessageExchangeFactory exchangeFactory;


    public ProviderEndpoint() {
    }

    public ProviderEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    public ProviderEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component.getServiceUnit(), endpoint.getServiceName(), endpoint.getEndpointName());
        logger = component.getLogger();
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.common.Endpoint#getRole()
     */
    public Role getRole() {
        return Role.PROVIDER;
    }

    public void activate() throws Exception {
        ComponentContext ctx = getServiceUnit().getComponent().getComponentContext();
        channel = ctx.getDeliveryChannel();
        exchangeFactory = channel.createExchangeFactory();
        activated = ctx.activateEndpoint(service, endpoint);
        start();
    }

    public void deactivate() throws Exception {
        stop();
        if (activated == null) {
            throw new IllegalStateException("Endpoint not activated: " + this);
        }
        ServiceEndpoint ep = activated;
        activated = null;
        ComponentContext ctx = getServiceUnit().getComponent().getComponentContext();
        ctx.deactivateEndpoint(ep);
    }

    public ExchangeProcessor getProcessor() {
        return this;
    }

    protected void send(MessageExchange me) throws MessagingException {
        if (me.getRole() == MessageExchange.Role.CONSUMER &&
            me.getStatus() == ExchangeStatus.ACTIVE) {
            BaseLifeCycle lf = (BaseLifeCycle) getServiceUnit().getComponent().getLifeCycle();
            lf.sendConsumerExchange(me, (Endpoint) this);
        } else {
            channel.send(me);
        }
    }
    
    protected void done(MessageExchange me) throws MessagingException {
        me.setStatus(ExchangeStatus.DONE);
        send(me);
    }
    
    protected void fail(MessageExchange me, Exception error) throws MessagingException {
        me.setError(error);
        send(me);
    }
    
    /**
     * @return the exchangeFactory
     */
    public MessageExchangeFactory getExchangeFactory() {
        return exchangeFactory;
    }

    public void start() throws Exception {
    }
    
    public void stop() throws Exception {
    }


    /**
     * A default implementation of the message processor which checks the status of the exchange
     * and if its valid will dispatch to either {@link #processInOnly(MessageExchange,NormalizedMessage)} for
     * an {@link InOnly} or {@link RobustInOnly} message exchange otherwise the
     * {@link #processInOut(MessageExchange,NormalizedMessage,NormalizedMessage)}
     * method will be invoked
     *
     * @param exchange the message exchange
     * @throws Exception
     */
    public void process(MessageExchange exchange) throws Exception {
        // The component acts as a provider, this means that another component has requested our service
        // As this exchange is active, this is either an in or a fault (out are sent by this component)
        if (exchange.getRole() == Role.PROVIDER) {
            // Exchange is finished
            if (exchange.getStatus() == ExchangeStatus.DONE) {
                return;
            // Exchange has been aborted with an exception
            } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                return;
            // Exchange is active
            } else {
                // In message
                NormalizedMessage in = exchange.getMessage("in");
                if (in != null) {
                    if (exchange instanceof InOnly || exchange instanceof RobustInOnly) {
                        processInOnly(exchange, in);
                    }
                    else {
                        NormalizedMessage out = exchange.getMessage("out");
                        if (out == null) {
                            out = exchange.createMessage();
                            exchange.setMessage(out, "out");
                        }
                        processInOut(exchange, in, out);
                    }
                    send(exchange);

                // Fault message
                } else if (exchange.getFault() != null) {
                    done(exchange);
                // This is not compliant with the default MEPs
                } else {
                    throw new IllegalStateException("Provider exchange is ACTIVE, but no in or fault is provided");
                }
            }
        // Unsupported role: this should never happen has we never create exchanges
        } else {
            throw new IllegalStateException("Unsupported role: " + exchange.getRole());
        }
    }


    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        throw new UnsupportedOperationException("Unsupported MEP: " + exchange.getPattern());
    }

    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        throw new UnsupportedOperationException("Unsupported MEP: " + exchange.getPattern());
    }
    
}
