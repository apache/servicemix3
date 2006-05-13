package ${packageName};

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.BaseLifeCycle;
import org.apache.servicemix.common.BaseServiceUnitManager;
import org.apache.servicemix.common.Deployer;

/**
 *
 */
public class MyComponent extends BaseComponent
{
    /* (non-Javadoc)
     * @see org.servicemix.common.BaseComponent#createLifeCycle()
     */
    protected BaseLifeCycle createLifeCycle() {
        return new MyLifeCycle(this);
    }

    /* (non-Javadoc)
     * @see org.servicemix.common.BaseComponent#createServiceUnitManager()
     */
    public BaseServiceUnitManager createServiceUnitManager() {
        Deployer[] deployers = new Deployer[] { new MyDeployer(this) };
        return new BaseServiceUnitManager(this, deployers);
    }

}
