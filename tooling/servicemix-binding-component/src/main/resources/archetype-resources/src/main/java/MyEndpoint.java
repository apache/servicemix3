package ${packageName};

import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.Definition;

import org.apache.servicemix.common.BaseLifeCycle;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.common.wsdl1.JbiExtension;
import org.apache.servicemix.soap.SoapEndpoint;

/**
 * @org.apache.xbean.XBean element="endpoint"
 */
public class MyEndpoint extends SoapEndpoint {

    /**
     * @org.apache.xbean.Property alias="role"
     * @param role
     */
    public void setRoleAsString(String role) {
        super.setRoleAsString(role);
    }

    public void validate() throws DeploymentException {
    }
    
    protected ExchangeProcessor createProviderProcessor() {
        return new MyProviderProcessor(this);
    }

    protected ExchangeProcessor createConsumerProcessor() {
        return new MyConsumerProcessor(this);
    }
    
    protected ServiceEndpoint createExternalEndpoint() {
        return new MyExternalEndpoint(this);
    }
    
    protected void overrideDefinition(Definition def) {
    }


}
