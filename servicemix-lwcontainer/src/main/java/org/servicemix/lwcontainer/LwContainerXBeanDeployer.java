package org.servicemix.lwcontainer;

import java.util.ArrayList;
import java.util.List;

import org.servicemix.common.BaseComponent;
import org.servicemix.common.xbean.AbstractXBeanDeployer;
import org.servicemix.jbi.container.ActivationSpec;
import org.servicemix.jbi.container.SpringServiceUnitContainer;
import org.xbean.kernel.Kernel;
import org.xbean.kernel.StringServiceName;

public class LwContainerXBeanDeployer extends AbstractXBeanDeployer {

    public LwContainerXBeanDeployer(BaseComponent component) {
        super(component);
    }

    protected String getXBeanFile() {
        return "servicemix";
    }

    protected List getServices(Kernel kernel) {
        try {
            Object jbi = kernel.getService(new StringServiceName("jbi"));
            SpringServiceUnitContainer suContainer = (SpringServiceUnitContainer) jbi; 
            ActivationSpec[] specs = suContainer.getActivationSpecs();
            List services = new ArrayList();
            for (int i = 0; i < specs.length; i++) {
                services.add(new LwContainerEndpoint(specs[i]));
            }
            return services;
        } catch (Exception e) {
            throw new RuntimeException("Can not find 'jbi' bean", e);
        }
    }

}
