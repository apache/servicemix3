package org.apache.servicemix.jbi.installation;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

public class Bootstrap1 implements Bootstrap {

    private static Bootstrap delegate;
    private static InstallationContext installContext;

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#cleanUp()
     */
    public void cleanUp() throws JBIException {
        Bootstrap1.delegate.cleanUp();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#getExtensionMBeanName()
     */
    public ObjectName getExtensionMBeanName() {
        return Bootstrap1.delegate.getExtensionMBeanName();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#init(javax.jbi.component.InstallationContext)
     */
    public void init(InstallationContext installContext) throws JBIException {
        Bootstrap1.installContext = installContext;
        Bootstrap1.delegate.init(installContext);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onInstall()
     */
    public void onInstall() throws JBIException {
        Bootstrap1.delegate.onInstall();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onUninstall()
     */
    public void onUninstall() throws JBIException {
        Bootstrap1.delegate.onUninstall();
    }

    /**
     * @return Returns the delegate.
     */
    public static Bootstrap getDelegate() {
        return Bootstrap1.delegate;
    }

    /**
     * @param delegate The delegate to set.
     */
    public static void setDelegate(Bootstrap delegate) {
        Bootstrap1.delegate = delegate;
    }

    /**
     * @return Returns the installContext.
     */
    public static InstallationContext getInstallContext() {
        return Bootstrap1.installContext;
    }
    
}
