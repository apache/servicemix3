package javax.jbi.component;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;

import javax.management.ObjectName;

public interface ComponentLifeCycle
{
    ObjectName getExtensionMBeanName();

    void init(ComponentContext context)
        throws JBIException;

    void shutDown()
        throws JBIException;

    void start()
        throws JBIException;

    void stop()
        throws JBIException;
}
