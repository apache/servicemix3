package ${packageName};

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.BaseLifeCycle;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.xbean.XBeanServiceUnit;

/**
 * 
 * @org.apache.xbean.XBean element="component"
 *                  description="My component"
 */
public class MySpringComponent extends BaseComponent {

    private MyEndpoint[] endpoints;

    /**
     * @return Returns the endpoints.
     */
    public MyEndpoint[] getEndpoints() {
        return endpoints;
    }

    /**
     * @param endpoints The endpoints to set.
     */
    public void setEndpoints(MyEndpoint[] endpoints) {
        this.endpoints = endpoints;
    }
    
    /* (non-Javadoc)
     * @see org.servicemix.common.BaseComponent#createLifeCycle()
     */
    protected BaseLifeCycle createLifeCycle() {
        return new LifeCycle();
    }

    /**
     * @author gnodet
     */
    public class LifeCycle extends MyLifeCycle {

        protected ServiceUnit su;
        
        public LifeCycle() {
            super(MySpringComponent.this);
        }
        
        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doInit()
         */
        protected void doInit() throws Exception {
            super.doInit();
            su = new XBeanServiceUnit();
            su.setComponent(MySpringComponent.this);
            for (int i = 0; i < endpoints.length; i++) {
                endpoints[i].setServiceUnit(su);
                endpoints[i].validate();
                su.addEndpoint(endpoints[i]);
            }
            getRegistry().registerServiceUnit(su);
        }

        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doStart()
         */
        protected void doStart() throws Exception {
            super.doStart();
            su.start();
        }
        
        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doStop()
         */
        protected void doStop() throws Exception {
            su.stop();
            super.doStop();
        }
        
        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doShutDown()
         */
        protected void doShutDown() throws Exception {
            su.shutDown();
            super.doShutDown();
        }
    }

}
