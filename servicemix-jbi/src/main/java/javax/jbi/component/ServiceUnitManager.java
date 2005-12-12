package javax.jbi.component;

import javax.jbi.management.DeploymentException;

public interface ServiceUnitManager
{
    String deploy(String serviceUnitName, String serviceUnitRootPath)
        throws DeploymentException;

    void init(String serviceUnitName, String serviceUnitRootPath)
        throws DeploymentException;

    void start(String serviceUnitName)
        throws DeploymentException;

    void stop(String serviceUnitName)
        throws DeploymentException;

    void shutDown(String serviceUnitName)
        throws DeploymentException;

    String undeploy(String serviceUnitName, String serviceUnitRootPath)
        throws DeploymentException;
}
