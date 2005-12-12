package javax.jbi.component;

import org.w3c.dom.DocumentFragment;

public interface InstallationContext
{
    String getComponentClassName();

    java.util.List getClassPathElements();

    String getComponentName();

    ComponentContext getContext();

    String getInstallRoot();

    DocumentFragment getInstallationDescriptorExtension();

    boolean isInstall();

    void setClassPathElements(java.util.List classPathElements);
}
