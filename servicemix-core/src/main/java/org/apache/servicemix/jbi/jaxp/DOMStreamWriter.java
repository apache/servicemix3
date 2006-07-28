/*
 * This implementation comes from the XFire project
 * https://svn.codehaus.org/xfire/trunk/xfire/xfire-core/src/main/org/codehaus/xfire/util/stax/
 */
package org.apache.servicemix.jbi.jaxp;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class DOMStreamWriter implements XMLStreamWriter
{

    public void close()
        throws XMLStreamException
    {
    }

    public void flush()
        throws XMLStreamException
    {
    }
}
