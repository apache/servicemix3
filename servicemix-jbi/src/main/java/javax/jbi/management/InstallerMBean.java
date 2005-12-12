package javax.jbi.management;

import javax.management.ObjectName;

public interface InstallerMBean
{
    String getInstallRoot();

    ObjectName install()
        throws javax.jbi.JBIException;

    boolean isInstalled();

    void uninstall()
        throws javax.jbi.JBIException;

    ObjectName getInstallerConfigurationMBean()
        throws javax.jbi.JBIException;
}
