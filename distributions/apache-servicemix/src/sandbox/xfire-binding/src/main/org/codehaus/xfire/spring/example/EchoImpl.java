package org.codehaus.xfire.spring.example;



/**
 * Provides a default implementation of the echo service interface.
 *
 * @author <a href="mailto:poutsma@mac.com">Arjen Poutsma</a>
 */
public class EchoImpl
        implements Echo
{
    public String echo(String in)
    {
        return in;
    }

}
