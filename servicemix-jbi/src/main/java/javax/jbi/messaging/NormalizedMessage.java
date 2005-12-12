package javax.jbi.messaging;

import java.util.Set;

import javax.activation.DataHandler;
import javax.security.auth.Subject;
import javax.xml.transform.Source;

public interface NormalizedMessage
{
    void addAttachment(String id, DataHandler content)
        throws MessagingException;
    
    Source getContent();
    
    DataHandler getAttachment(String id);
    
    Set getAttachmentNames();
    
    void removeAttachment(String id)
        throws MessagingException;
    
    void setContent(Source content)
        throws MessagingException;
    
    void setProperty(String name, Object value);
    
    void setSecuritySubject(Subject subject);
    
    Set getPropertyNames();
        
    Object getProperty(String name);
    
    Subject getSecuritySubject();
}
