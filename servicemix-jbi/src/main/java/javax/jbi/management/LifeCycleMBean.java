package javax.jbi.management;

import javax.jbi.JBIException;

public interface LifeCycleMBean
{
    void start() throws JBIException;

    void stop() throws JBIException;

    void shutDown() throws JBIException;

    String getCurrentState();

    final static String SHUTDOWN = "Shutdown";

    final static String STOPPED  = "Stopped";

    final static String RUNNING  = "Running";

    final static String UNKNOWN  = "Unknown";
}
