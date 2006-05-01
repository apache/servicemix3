package org.apache.geronimo.servicemix;

import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class Component implements GBeanLifecycle {

    private static final Log log = LogFactory.getLog(Component.class);
    
    private String name;
    private String description;
    private String type;
	private String className;
	private Container container;
	private URI rootDir;
	private URI installDir;
	private URI workDir;
	private javax.jbi.component.Component component;
	private ClassLoader classLoader;
	
	public Component(String name, 
					 String description,
					 String type, 
					 String className, 
					 Container container,
					 URL configurationBaseUrl,
					 ClassLoader classLoader) throws Exception {
		this.name = name;
		this.description = description;
		this.type = type;
		this.className = className;
		this.container = container;
        //TODO is there a simpler way to do this?
        if (configurationBaseUrl.getProtocol().equalsIgnoreCase("file")) {
        	this.rootDir = new URI("file", configurationBaseUrl.getPath(), null);
        } else {
        	this.rootDir = URI.create(configurationBaseUrl.toString());
        }
        this.installDir = rootDir.resolve("install/");
        this.workDir = rootDir.resolve("workspace/");
        this.classLoader = classLoader;
        log.info("Created JBI component: " + name);
	}
	
	public void doStart() throws Exception {
        log.info("doStart called for JBI component: " + name);
        try {
	        component = (javax.jbi.component.Component) classLoader.loadClass(className).newInstance();
	        container.register(this);
        } catch (ClassNotFoundException e) {
        	log.error(classLoader);
        }
	}

	public void doStop() throws Exception {
        log.info("doStop called for JBI component: " + name);
        container.unregister(this);
        component = null;
	}

	public void doFail() {
        log.info("doFail called for JBI component: " + name);
        component = null;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public URI getInstallDir() {
		return installDir;
	}

	public URI getWorkDir() {
		return workDir;
	}

	public URI getRootDir() {
		return rootDir;
	}

	public javax.jbi.component.Component getComponent() {
		return component;
	}
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("JBIComponent", Component.class, "JBIComponent");
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("description", String.class, true);
        infoFactory.addAttribute("type", String.class, true);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addReference("container", Container.class);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.setConstructor(new String[] {"name", "description", "type", "className", "container", "configurationBaseUrl", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
