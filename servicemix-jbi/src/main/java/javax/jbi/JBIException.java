package javax.jbi;

public class JBIException extends Exception
{
    public JBIException(String aMessage)
    {
        super(aMessage);
    }

    public JBIException(String aMessage, Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public JBIException(Throwable aCause)
    {
        super(aCause);
    }
}
