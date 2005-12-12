package javax.jbi.component;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public interface Component
{
    ComponentLifeCycle getLifeCycle();
    
    ServiceUnitManager getServiceUnitManager();

    Document getServiceDescription(ServiceEndpoint endpoint);
    
    boolean isExchangeWithConsumerOkay(
        ServiceEndpoint endpoint,
        MessageExchange exchange);
    
    boolean isExchangeWithProviderOkay(
        ServiceEndpoint endpoint,
        MessageExchange exchange);
    
    ServiceEndpoint resolveEndpointReference(DocumentFragment epr);
}
