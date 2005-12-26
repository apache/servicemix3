package org.apache.servicemix.components.xbean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.SortedSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.servicemix.components.xbean.XBeanComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;

import junit.framework.TestCase;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class XBeanComponentTest extends TestCase {
    
    private static final String CLASS_NAME = "TestClass";
    private static final String ENTRY_NAME = "foo";
    private static final String ENTRY_VALUE = "bar";
    
    public void testXBeanJbi() throws Exception {
        createJarFile();
        File path = new File("target/test-classes/xbean");
        XBeanComponent component = new XBeanComponent();
        JBIContainer container = new JBIContainer();
        container.init();
        container.start();
        container.activateComponent(new ActivationSpec("xbean", component));
        component.getServiceUnitManager().deploy("xbean", path.getAbsolutePath());
        component.start("xbean");
        component.stop("xbean");
        component.getServiceUnitManager().shutDown("xbean");
        component.getServiceUnitManager().undeploy("xbean", path.getAbsolutePath());
        component.getServiceUnitManager().deploy("xbean", path.getAbsolutePath());
    }

    private static File createJarFile() throws IOException {
        File file = new File("target/test-classes/xbean/SpringLoaderTest.jar");

        FileOutputStream out = new FileOutputStream(file);
        JarOutputStream jarOut = new JarOutputStream(out);

        jarOut.putNextEntry(new JarEntry(CLASS_NAME + ".class"));
        jarOut.write(createClass(CLASS_NAME));

        jarOut.putNextEntry(new JarEntry(ENTRY_NAME));
        jarOut.write(ENTRY_VALUE.getBytes());

        jarOut.close();
        out.close();

        return file;
    }

    private static byte[] createClass(final String name) {
        Enhancer enhancer = new Enhancer();
        enhancer.setNamingPolicy(new NamingPolicy() {
            public String getClassName(String prefix, String source, Object key, Predicate names) {
                return name;
            }
        });
        enhancer.setClassLoader(new URLClassLoader(new URL[0]));
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(new Class[]{SortedSet.class});
        enhancer.setCallbackTypes(new Class[]{NoOp.class});
        enhancer.setUseFactory(false);
        ByteCode byteCode = new ByteCode();
        enhancer.setStrategy(byteCode);
        enhancer.createClass();

        return byteCode.getByteCode();
    }

    private static class ByteCode extends DefaultGeneratorStrategy {
        private byte[] byteCode;

        public byte[] transform(byte[] byteCode) {
            this.byteCode = byteCode;
            return byteCode;
        }

        public byte[] getByteCode() {
            return byteCode;
        }
    }
}
