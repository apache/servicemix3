package javax.jbi.management;

public interface DeploymentServiceMBean
{
    
    String deploy (String saZipURL) throws Exception;

    String undeploy (String saName) throws Exception;

    String[] getDeployedServiceUnitList (String componentName) throws Exception;

    String[] getDeployedServiceAssemblies () throws Exception;
    
    String getServiceAssemblyDescriptor (String saName) throws Exception;
   
    String[] getDeployedServiceAssembliesForComponent (String componentName)
        throws Exception;
    
    String[] getComponentsForDeployedServiceAssembly (String saName) throws Exception;
    
    boolean isDeployedServiceUnit (String componentName, String suName) throws Exception;
    
    boolean canDeployToComponent (String componentName);
    
    String start(String serviceAssemblyName) throws Exception;
    
    String stop(String serviceAssemblyName) throws Exception;
    
    String shutDown(String serviceAssemblyName) throws Exception;
    
    String getState(String serviceAssemblyName) throws Exception;

    static final String STARTED   = "Started";
    
    static final String SHUTDOWN = "Shutdown";

    static final String STOPPED   = "Stopped";
}
