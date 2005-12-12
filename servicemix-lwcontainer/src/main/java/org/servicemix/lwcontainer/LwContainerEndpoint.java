package org.servicemix.lwcontainer;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange.Role;
import javax.xml.namespace.QName;

import org.activemq.util.IdGenerator;
import org.servicemix.common.Endpoint;
import org.servicemix.common.ExchangeProcessor;
import org.servicemix.jbi.container.ActivationSpec;
import org.servicemix.jbi.container.JBIContainer;
import org.servicemix.jbi.framework.ComponentContextImpl;

public class LwContainerEndpoint extends Endpoint {

    private static final QName SERVICE_NAME = new QName("http://lwcontainer.servicemix.org", "LwContainerComponent");
    
    private ActivationSpec activationSpec;
    
    public LwContainerEndpoint(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
        this.service = SERVICE_NAME;
        if (activationSpec.getId() != null) {
            this.endpoint = activationSpec.getId();
        } else if (activationSpec.getComponentName() != null) {
            this.endpoint = activationSpec.getComponentName();
        } else {
            this.endpoint = new IdGenerator().generateId();
        }
    }
    
    public Role getRole() {
        throw new UnsupportedOperationException();
    }

    public void activate() throws Exception {
        getContainer().activateComponent(activationSpec);
        
    }

    public void deactivate() throws Exception {
        getContainer().deactivateComponent(activationSpec.getId());
    }

    public ExchangeProcessor getProcessor() {
        throw new UnsupportedOperationException();
    }

    public JBIContainer getContainer() {
        ComponentContext context = getServiceUnit().getComponent().getComponentContext();
        if( context instanceof ComponentContextImpl ) {
            return ((ComponentContextImpl) context).getContainer();
        }
        throw new IllegalStateException("LwContainer component can only be deployed in ServiceMix");
    }

}
