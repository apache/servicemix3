package org.apache.servicemix.components.util;

import java.io.Reader;
import java.io.StringReader;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.xml.sax.InputSource;

public class CopyTransformerTest extends TestCase {

    private CopyTransformer transformer = CopyTransformer.getInstance();
    
    public void testWithSAXSource() throws Exception {
        Reader r = new StringReader("<hello>world</hello>");
        Source src = new SAXSource(new InputSource(r));
        NormalizedMessage msg = copyMessage(src);
        r.close();
        new SourceTransformer().contentToString(msg);
    }
    
    public void testWithStreamSource() throws Exception {
        Reader r = new StringReader("<hello>world</hello>");
        Source src = new StreamSource(r);
        NormalizedMessage msg = copyMessage(src);
        r.close();
        new SourceTransformer().contentToString(msg);
    }
    
    protected NormalizedMessage copyMessage(Source src) throws Exception {
        NormalizedMessage from = new NormalizedMessageImpl();
        NormalizedMessage to = new NormalizedMessageImpl();
        from.setContent(src);
        transformer.transform(null, from, to);
        return to;
    }
    
}
