package org.apache.servicemix.common;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;

public abstract class ProviderEndpoint extends Endpoint implements ExchangeProcessor {

    private ServiceEndpoint activated;
    private DeliveryChannel channel;
    private MessageExchangeFactory exchangeFactory;

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

}
