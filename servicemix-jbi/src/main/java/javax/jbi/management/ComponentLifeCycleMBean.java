package javax.jbi.management;

import javax.management.ObjectName;

public interface ComponentLifeCycleMBean extends LifeCycleMBean
{
    ObjectName getExtensionMBeanName() throws javax.jbi.JBIException;
}
