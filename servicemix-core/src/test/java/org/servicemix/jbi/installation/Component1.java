package org.servicemix.jbi.installation;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

public class Component1 implements Component {

    private static Component delegate;

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getLifeCycle()
     */
    public ComponentLifeCycle getLifeCycle() {
        return delegate.getLifeCycle();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getServiceDescription(javax.jbi.servicedesc.ServiceEndpoint)
     */
    public Document getServiceDescription(ServiceEndpoint endpoint) {
        return delegate.getServiceDescription(endpoint);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getServiceUnitManager()
     */
    public ServiceUnitManager getServiceUnitManager() {
        return delegate.getServiceUnitManager();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#isExchangeWithConsumerOkay(javax.jbi.servicedesc.ServiceEndpoint, javax.jbi.messaging.MessageExchange)
     */
    public boolean isExchangeWithConsumerOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return delegate.isExchangeWithConsumerOkay(endpoint, exchange);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#isExchangeWithProviderOkay(javax.jbi.servicedesc.ServiceEndpoint, javax.jbi.messaging.MessageExchange)
     */
    public boolean isExchangeWithProviderOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return delegate.isExchangeWithProviderOkay(endpoint, exchange);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#resolveEndpointReference(org.w3c.dom.DocumentFragment)
     */
    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        return delegate.resolveEndpointReference(epr);
    }

    /**
     * @return Returns the delegate.
     */
    public static Component getDelegate() {
        return delegate;
    }

    /**
     * @param delegate The delegate to set.
     */
    public static void setDelegate(Component delegate) {
        Component1.delegate = delegate;
    }

}
