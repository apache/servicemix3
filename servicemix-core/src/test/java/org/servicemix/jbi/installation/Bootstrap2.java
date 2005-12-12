package org.servicemix.jbi.installation;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

public class Bootstrap2 implements Bootstrap {

    private static Bootstrap delegate;

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#cleanUp()
     */
    public void cleanUp() throws JBIException {
        delegate.cleanUp();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#getExtensionMBeanName()
     */
    public ObjectName getExtensionMBeanName() {
        return delegate.getExtensionMBeanName();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#init(javax.jbi.component.InstallationContext)
     */
    public void init(InstallationContext installContext) throws JBIException {
        delegate.init(installContext);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onInstall()
     */
    public void onInstall() throws JBIException {
        delegate.onInstall();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onUninstall()
     */
    public void onUninstall() throws JBIException {
        delegate.onUninstall();
    }

    /**
     * @return Returns the delegate.
     */
    public static Bootstrap getDelegate() {
        return delegate;
    }

    /**
     * @param delegate The delegate to set.
     */
    public static void setDelegate(Bootstrap delegate) {
        Bootstrap2.delegate = delegate;
    }
    
}
