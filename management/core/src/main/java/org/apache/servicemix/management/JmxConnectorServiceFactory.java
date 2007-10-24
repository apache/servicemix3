package org.apache.servicemix.management;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.support.ConnectorServerFactoryBean;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 14, 2007
 * Time: 11:10:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JmxConnectorServiceFactory implements ManagedServiceFactory, DisposableBean {

    private Map<String, ConnectorServerFactoryBean> connectors = new HashMap<String, ConnectorServerFactoryBean>();
    private Properties environment;

    public void setEnvironment(Properties environment) {
        this.environment = environment;
    }

    public String getName() {
        return JmxConnectorServiceFactory.class.getName();
    }

    public void updated(final String s, final Dictionary dictionary) throws ConfigurationException {
        System.err.println("Updated: " + s + " with props (" + dictionary + ")");
        try {
            if (connectors.containsKey(s)) {
                deleted(s);
            }
            ConnectorServerFactoryBean factory = new ConnectorServerFactoryBean();
            Map<String, Object> props = new HashMap<String, Object>();
            for (Enumeration e = dictionary.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                props.put(key, dictionary.get(key));
            }
            factory.setEnvironment(environment);
            IntrospectionSupport.setProperties(factory, props);
            factory.afterPropertiesSet();
            connectors.put(s, factory);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new ConfigurationException("Unable to create service", "Unknown", e);
        }
    }

    public void deleted(String s) {
        System.err.println("Deleted: " + s);
        try {
            ConnectorServerFactoryBean factory = connectors.remove(s);
            if (factory != null) {
                factory.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void destroy() throws Exception {
        while (!connectors.isEmpty()) {
            deleted(connectors.keySet().iterator().next());
        }
    }
}
