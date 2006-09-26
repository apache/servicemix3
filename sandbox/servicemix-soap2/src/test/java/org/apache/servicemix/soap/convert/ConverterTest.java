package org.apache.servicemix.soap.convert;

import org.apache.woden.tool.converter.Convert;

import junit.framework.TestCase;

public class ConverterTest extends TestCase {

    public void test() throws Exception {
        String url = getClass().getResource("person.wsdl").toString();
        Convert.main(new String[] { "-wsdl", url, "-dir", "target/", "-overwrite", "on"});
    }
    
}
