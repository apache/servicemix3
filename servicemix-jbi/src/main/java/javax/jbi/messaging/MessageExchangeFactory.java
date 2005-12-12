package javax.jbi.messaging;

import java.net.URI;

import javax.xml.namespace.QName;

public interface MessageExchangeFactory
{
    MessageExchange createExchange(QName serviceName, QName operationName)
        throws MessagingException;
    
    MessageExchange createExchange(URI pattern)
        throws MessagingException;
        
    InOnly createInOnlyExchange()
        throws MessagingException;
    
    InOptionalOut createInOptionalOutExchange()
        throws MessagingException;
    
    InOut createInOutExchange()
        throws MessagingException;
    
    RobustInOnly createRobustInOnlyExchange()
        throws MessagingException;    
}
