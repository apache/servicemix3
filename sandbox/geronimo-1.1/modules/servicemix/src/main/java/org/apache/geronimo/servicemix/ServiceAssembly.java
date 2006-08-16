package org.apache.geronimo.servicemix;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.servicemix.jbi.deployment.Descriptor;
import org.apache.servicemix.jbi.deployment.DescriptorFactory;

public class ServiceAssembly implements GBeanLifecycle {

    private static final Log log = LogFactory.getLog(ServiceAssembly.class);
    
    private String name;
	private Container container;
	private URI rootDir;
    
	public ServiceAssembly(String name, 
			               Container container,
			               URL configurationBaseUrl) throws Exception {
		this.name = name;
		this.container = container;
        //TODO is there a simpler way to do this?
        if (configurationBaseUrl.getProtocol().equalsIgnoreCase("file")) {
        	this.rootDir = new URI("file", configurationBaseUrl.getPath(), null);
        } else {
        	this.rootDir = URI.create(configurationBaseUrl.toString());
        }
        log.info("Created JBI service assembly: " + name);
	}
	
	public void doStart() throws Exception {
        log.info("doStart called for JBI service assembly: " + name);
        container.register(this);
	}

	public void doStop() throws Exception {
        log.info("doStop called for JBI service assembly: " + name);
        container.unregister(this);
	}

	public void doFail() {
        log.info("doFail called for JBI service assembly: " + name);
	}
	
	public URI getRootDir() {
		return rootDir;
	}
	
	public String getName() {
		return name;
	}
	
	public Descriptor getDescriptor() throws Exception {
		return DescriptorFactory.buildDescriptor(new File(new File(rootDir), "install"));
	}

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("JBIServiceAssembly", ServiceAssembly.class, "JBIServiceAssembly");
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addReference("container", Container.class);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);
        infoFactory.setConstructor(new String[] {"name", "container", "configurationBaseUrl" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
