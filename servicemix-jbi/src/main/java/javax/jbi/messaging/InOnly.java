package javax.jbi.messaging;

public interface InOnly extends MessageExchange
{
    NormalizedMessage getInMessage();
        
    void setInMessage(NormalizedMessage msg)
        throws MessagingException;
}
