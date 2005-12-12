package org.servicemix.lwcontainer;

import org.servicemix.common.BaseComponent;
import org.servicemix.common.BaseServiceUnitManager;
import org.servicemix.common.Deployer;

public class LwContainerComponent extends BaseComponent {

    /* (non-Javadoc)
     * @see org.servicemix.common.BaseComponent#createServiceUnitManager()
     */
    public BaseServiceUnitManager createServiceUnitManager() {
        Deployer[] deployers = new Deployer[] { new LwContainerXBeanDeployer(this) };
        return new BaseServiceUnitManager(this, deployers);
    }

}
