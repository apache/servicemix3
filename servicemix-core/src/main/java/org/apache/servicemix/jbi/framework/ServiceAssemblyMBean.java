package org.apache.servicemix.jbi.framework;

import javax.jbi.management.DeploymentServiceMBean;
import javax.management.ObjectName;


public interface ServiceAssemblyMBean {

    public static final String STARTED = DeploymentServiceMBean.STARTED;

    public static final String SHUTDOWN = DeploymentServiceMBean.SHUTDOWN;

    public static final String STOPPED = DeploymentServiceMBean.STOPPED;

    public String getName();
    
    public String getDescription();
    
    public String getCurrentState();
    
    public String getDescriptor();
    
    public ObjectName[] getServiceUnits();

    public String start() throws Exception;

    public String stop() throws Exception;

    public String shutDown() throws Exception;
}
