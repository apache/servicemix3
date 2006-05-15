package ${packageName};

import javax.jbi.management.DeploymentException;

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.xbean.AbstractXBeanDeployer;

public class MyDeployer extends AbstractXBeanDeployer {

    public MyDeployer(BaseComponent component) {
        super(component);
    }

    protected boolean validate(Endpoint endpoint) throws DeploymentException {
        if (endpoint instanceof MyEndpoint == false) {
            throw new DeploymentException("Endpoint should be a MyEndpoint");
        }
        ((MyEndpoint) endpoint).validate();
        return true;
    }
}
