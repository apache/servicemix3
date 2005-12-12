package org.servicemix.gbean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class ServiceMixDeployment implements GBeanLifecycle {

    private Log log = LogFactory.getLog(getClass().getName());
    
    private String name;
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ServiceMix Deployment", ServiceMixDeployment.class, "JBIDeployment");
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.setConstructor(new String[]{"name"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
    public ServiceMixDeployment(String name) {
        this.name = name;
    }

    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
        log.info("Starting JBI deployment: " + name );
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        log.info("Stopping JBI deployment: " + name);
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        log.info("Failing JBI deployment: " + name);
    }

}
