package javax.jbi.component;

import javax.jbi.JBIException;

import javax.management.ObjectName;

public interface Bootstrap
{
    void init(InstallationContext installContext)
        throws JBIException;

    void cleanUp() throws JBIException;

    ObjectName getExtensionMBeanName();

    void onInstall()
        throws JBIException;

    void onUninstall()
        throws JBIException;
}
