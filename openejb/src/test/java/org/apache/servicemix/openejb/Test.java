package org.apache.servicemix.openejb;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Nov 14, 2007
 * Time: 12:31:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test extends TestCase {

    public void test() throws Exception {
        System.setProperty("openejb.deployments.classpath", "false");

        OsgiWsRegistry registry = new OsgiWsRegistry();
        OpenEjbFactory factory = new OpenEjbFactory();
        factory.setWsRegistry(registry);
        factory.init();

        File f = new File("target/test-classes");
        URL url = f.getAbsoluteFile().toURL();
        URLClassLoader cl = new URLClassLoader(new URL[] {url}, Test.class.getClassLoader());
        System.out.println(url.toString());
        new Deployer().deploy(cl, url.toString());
    }

}
