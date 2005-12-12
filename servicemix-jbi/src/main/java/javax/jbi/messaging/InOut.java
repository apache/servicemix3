package javax.jbi.messaging;

public interface InOut extends MessageExchange
{   
    NormalizedMessage getInMessage();
    
    NormalizedMessage getOutMessage();
        
    void setInMessage(NormalizedMessage msg)
        throws MessagingException;
        
    void setOutMessage(NormalizedMessage msg)
        throws MessagingException;
}
