package javax.jbi.messaging;

public class MessagingException extends javax.jbi.JBIException
{
    public MessagingException(String msg)
    {
        super(msg);
    }
    
    public MessagingException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    public MessagingException(Throwable cause)
    {
        super(cause);
    }
}
