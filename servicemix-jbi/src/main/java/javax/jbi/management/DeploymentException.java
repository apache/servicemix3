package javax.jbi.management;

public class DeploymentException extends javax.jbi.JBIException
{
    public DeploymentException(String aMessage)
    {
        super(aMessage);
    }

    public DeploymentException(String aMessage, Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public DeploymentException(Throwable aCause)
    {
        super(aCause);
    }
}
