package javax.jbi.management;

import javax.management.ObjectName;

public interface AdminServiceMBean
{
    ObjectName[] getBindingComponents();

    ObjectName getComponentByName(String name);

    ObjectName[] getEngineComponents();

    String getSystemInfo();

    ObjectName getSystemService(String serviceName);

    ObjectName[] getSystemServices();

    boolean isBinding(String componentName);

    boolean isEngine(String componentName);
}
