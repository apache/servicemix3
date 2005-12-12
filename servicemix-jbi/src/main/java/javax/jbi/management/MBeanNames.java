package javax.jbi.management;

import javax.management.ObjectName;

public interface MBeanNames
{
    ObjectName createCustomComponentMBeanName(String customName);

    static final String BOOTSTRAP_EXTENSION = "BootstrapExtension";

    static final String COMPONENT_LIFE_CYCLE_EXTENSION = "LifeCycleExtension";

    String getJmxDomainName();
}
