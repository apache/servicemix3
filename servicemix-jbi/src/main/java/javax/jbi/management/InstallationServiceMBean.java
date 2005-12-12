package javax.jbi.management;

import javax.management.ObjectName;

public interface InstallationServiceMBean
{
    ObjectName loadNewInstaller(String installJarURL);

    ObjectName loadInstaller(String aComponentName);

    boolean
    unloadInstaller(String aComponentName, boolean isToBeDeleted);

    String installSharedLibrary(String aSharedLibURI);

    boolean uninstallSharedLibrary(String aSharedLibName);
}
