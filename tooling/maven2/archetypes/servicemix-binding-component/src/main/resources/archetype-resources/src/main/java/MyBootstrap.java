package ${packageName};

import org.apache.servicemix.common.BaseBootstrap;

/**
 * Bootstrap class.
 * This class is usefull to perform tasks at installation / uninstallation time 
 */
public class MyBootstrap extends BaseBootstrap
{

    protected void doInit() throws Exception {
        super.doInit();
    }
    
    protected void doCleanUp() throws Exception {
        super.doCleanUp();
    }
 
    protected void doOnInstall() throws Exception {
        super.doOnInstall();
    }
 
    protected void doOnUninstall() throws Exception {
        super.doOnUninstall();
    }
 
    protected Object getExtensionMBean() throws Exception {
        return null;
    }
    
}
