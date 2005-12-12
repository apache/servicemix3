package javax.jbi.messaging;

public interface RobustInOnly extends MessageExchange
{    
    NormalizedMessage getInMessage();
    
    void setInMessage(NormalizedMessage msg)
        throws MessagingException;
}
