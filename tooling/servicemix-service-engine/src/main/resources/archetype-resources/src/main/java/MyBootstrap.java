package ${packageName};

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Bootstrap class.
 * This class is usefull to perform tasks at installation / uninstallation time 
 */
public class MyBootstrap implements Bootstrap
{

    protected InstallationContext context;
    protected ObjectName mbeanName;
    
    public MyBootstrap() {
    }
    
    public ObjectName getExtensionMBeanName() {
        return mbeanName;
    }

    protected Object getExtensionMBean() throws Exception {
        return null;
    }
    
    protected ObjectName createExtensionMBeanName() throws Exception {
        return this.context.getContext().getMBeanNames().createCustomComponentMBeanName("bootstrap");
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#init(javax.jbi.component.InstallationContext)
     */
    public void init(InstallationContext installContext) throws JBIException {
        try {
            this.context = installContext;
            doInit();
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling init", e);
        }
    }

    protected void doInit() throws Exception {
        Object mbean = getExtensionMBean();
        if (mbean != null) {
            this.mbeanName = createExtensionMBeanName();
            MBeanServer server = this.context.getContext().getMBeanServer();
            if (server == null) {
                throw new JBIException("null mBeanServer");
            }
            if (server.isRegistered(this.mbeanName)) {
                server.unregisterMBean(this.mbeanName);
            }
            server.registerMBean(mbean, this.mbeanName);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#cleanUp()
     */
    public void cleanUp() throws JBIException {
        try {
            doCleanUp();
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling cleanUp", e);
        }
    }

    protected void doCleanUp() throws Exception {
        if (this.mbeanName != null) {
            MBeanServer server = this.context.getContext().getMBeanServer();
            if (server == null) {
                throw new JBIException("null mBeanServer");
            }
            if (server.isRegistered(this.mbeanName)) {
                server.unregisterMBean(this.mbeanName);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onInstall()
     */
    public void onInstall() throws JBIException {
        try {
            doOnInstall();
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling onInstall", e);
        }
    }

    protected void doOnInstall() throws Exception {
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onUninstall()
     */
    public void onUninstall() throws JBIException {
        try {
            doOnUninstall();
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling onUninstall", e);
        }
    }

    protected void doOnUninstall() throws Exception {
    }
    
}
