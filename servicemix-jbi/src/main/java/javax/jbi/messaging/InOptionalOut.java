package javax.jbi.messaging;

public interface InOptionalOut extends MessageExchange
{
    NormalizedMessage getInMessage();
    
    NormalizedMessage getOutMessage();
    
    void setInMessage(NormalizedMessage msg)
        throws MessagingException;
    
    void setOutMessage(NormalizedMessage msg)
        throws MessagingException;
}
