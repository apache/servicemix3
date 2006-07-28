package ${packageName};

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    protected final transient Log logger = LogFactory.getLog(getClass());
    
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
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing bootstrap");
            }
            this.context = installContext;
            doInit();
            if (logger.isDebugEnabled()) {
                logger.debug("Bootstrap initialized");
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("Cleaning up bootstrap");
            }
            doCleanUp();
            if (logger.isDebugEnabled()) {
                logger.debug("Bootstrap cleaned up");
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("Bootstrap onInstall");
            }
            doOnInstall();
            if (logger.isDebugEnabled()) {
                logger.debug("Bootstrap onInstall done");
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("Bootstrap onUninstall");
            }
            doOnUninstall();
            if (logger.isDebugEnabled()) {
                logger.debug("Bootstrap onUninstall done");
            }
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling onUninstall", e);
        }
    }

    protected void doOnUninstall() throws Exception {
    }
    
}
