package org.apache.servicemix.core;

import org.apache.servicemix.api.service.ServiceRegistry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Aug 27, 2007
 * Time: 12:43:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceRegistryImpl<T> implements ServiceRegistry<T> {

    private Map<T, Map<String, ?>> registry = new ConcurrentHashMap<T, Map<String, ?>>();

    public void register(T service, Map<String, ?> properties) {
        registry.put(service, properties);
    }

    public void unregister(T service) {
        registry.remove(service);
    }

    public Set<T> getServices() {
        return registry.keySet();
    }

    public Map<String, ?> getProperties(T service) {
        return registry.get(service);
    }
}
