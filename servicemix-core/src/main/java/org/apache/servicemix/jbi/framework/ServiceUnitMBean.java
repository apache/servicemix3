package org.apache.servicemix.jbi.framework;

import javax.jbi.management.DeploymentServiceMBean;

public interface ServiceUnitMBean {

    public String getName();
    
    public String getDescription();
    
    public String getComponentName();

    public String getCurrentState();
    
    public String getServiceAssembly();
    
    public String getDescriptor();

    public static final String STARTED = DeploymentServiceMBean.STARTED;

    public static final String SHUTDOWN = DeploymentServiceMBean.SHUTDOWN;

    public static final String STOPPED = DeploymentServiceMBean.STOPPED;
}
