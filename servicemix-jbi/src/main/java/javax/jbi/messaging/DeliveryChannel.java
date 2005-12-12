package javax.jbi.messaging;

import javax.jbi.servicedesc.ServiceEndpoint;

import javax.xml.namespace.QName;

public interface DeliveryChannel
{    
    void close()
        throws MessagingException;
    
    MessageExchangeFactory createExchangeFactory();
    
    MessageExchangeFactory createExchangeFactory(QName interfaceName);
    
    MessageExchangeFactory createExchangeFactoryForService(QName serviceName);
    
    MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint);
      
    MessageExchange accept()
        throws MessagingException;
    
    MessageExchange accept(long timeout)
        throws MessagingException;   
    
    void send(MessageExchange exchange)
        throws MessagingException;
    
    boolean sendSync(MessageExchange exchange)
        throws MessagingException;
    
    boolean sendSync(MessageExchange exchange, long timeout)
        throws MessagingException;    
}
