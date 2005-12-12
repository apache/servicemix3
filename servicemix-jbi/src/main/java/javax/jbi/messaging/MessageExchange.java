package javax.jbi.messaging;

import java.net.URI;

import javax.jbi.servicedesc.ServiceEndpoint;

import javax.xml.namespace.QName;

public interface MessageExchange
{
    String JTA_TRANSACTION_PROPERTY_NAME = "javax.jbi.transaction.jta";
    
    URI getPattern();
    
    String getExchangeId();
    
    ExchangeStatus getStatus();
        
    void setStatus(ExchangeStatus status)
        throws MessagingException;
        
    void setError(Exception error);
    
    Exception getError();
    
    Fault getFault();
        
    void setFault(Fault fault)
        throws MessagingException;
    
    NormalizedMessage createMessage()
        throws MessagingException;
    
    Fault createFault()
        throws MessagingException;
        
    NormalizedMessage getMessage(String name);
        
    void setMessage(NormalizedMessage msg, String name)
        throws MessagingException;
    
    Object getProperty(String name);
    
    void setProperty(String name, Object obj);
    
    void setEndpoint(ServiceEndpoint endpoint);
    
    void setService(QName service);
    
    void setInterfaceName(QName interfaceName);
    
    void setOperation(QName name);
    
    ServiceEndpoint getEndpoint();
    
    QName getInterfaceName();
    
    QName getService();
    
    QName getOperation();
    
    boolean isTransacted();

    Role getRole();
        
    java.util.Set getPropertyNames();

    public static final class Role
    {
        public static final Role PROVIDER = new Role();

        public static final Role CONSUMER = new Role();

        private Role()
        {
        }
    }
}
