/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jbi.deployer.impl;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.component.Component;

import org.apache.servicemix.jbi.deployer.SharedLibrary;
import org.apache.servicemix.jbi.deployer.descriptor.Descriptor;
import org.apache.servicemix.jbi.deployer.descriptor.DescriptorFactory;
import org.apache.servicemix.jbi.deployer.descriptor.SharedLibraryList;
import org.apache.xbean.classloader.JarFileClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.springframework.osgi.internal.context.support.BundleDelegatingClassLoader;

/**
 * Deployer for JBI artifacts
 *
 */
public class Deployer implements BundleListener {

    private static final String JBI_DESCRIPTOR = "META-INF/jbi.xml";

    private static final String NAME = "NAME";
    private static final String TYPE = "TYPE";

    private BundleContext context;

    private Map<String, SharedLibrary> sharedLibraries;

    public Deployer() {
        sharedLibraries = new ConcurrentHashMap<String, SharedLibrary>();
    }

    public void setBundleContext(BundleContext context) {
        this.context = context;
    }

    public void bundleChanged(BundleEvent event) {
        try {
            if (event.getType() == BundleEvent.INSTALLED) {
                URL url = event.getBundle().getResource(JBI_DESCRIPTOR);
                Descriptor descriptor = DescriptorFactory.buildDescriptor(url);
                // TODO: check descriptor
                if (descriptor.getComponent() != null) {
                    // Create component class loader
                    ClassLoader classLoader = createComponentClassLoader(descriptor.getComponent(), event.getBundle());
                    // Instanciate component
                    Class clazz = classLoader.loadClass(descriptor.getComponent().getComponentClassName());
                    Component component = (Component) clazz.newInstance();
                    Dictionary<String, String> props = new Hashtable<String, String>();
                    // populate props from the component meta-data
                    props.put(NAME, descriptor.getComponent().getIdentification().getName());
                    props.put(TYPE, descriptor.getComponent().getType());
                    // register the component in the OSGi registry
                    context.registerService(Component.class.getName(), component, props);
                } else if (descriptor.getServiceAssembly() != null) {
                    // TODO:
                } else if (descriptor.getSharedLibrary() != null) {
                    SharedLibraryImpl sl = new SharedLibraryImpl(descriptor.getSharedLibrary(), event.getBundle());
                    sharedLibraries.put(sl.getName(), sl);
                    //context.registerService(SharedLibrary.class.getName(), sl, new Properties());
                } else {
                    // WARN: unhandled JBI artifact
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ClassLoader createComponentClassLoader(org.apache.servicemix.jbi.deployer.descriptor.Component component, Bundle bundle) {
        // Create parents classloaders
        ClassLoader[] parents;
        if (component.getSharedLibraries() != null) {
            parents = new ClassLoader[component.getSharedLibraries().length + 1];
            for (int i = 0; i < component.getSharedLibraries().length; i++) {
                parents[i + 1] = getSharedLibraryClassLoader(component.getSharedLibraries()[i]);
            }
        } else {
            parents = new ClassLoader[1];
        }
        parents[0] = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, null);

        // Create urls
        String[] classPathNames = component.getComponentClassPath().getPathElements();
        URL[] urls = new URL[classPathNames.length];
        for (int i = 0; i < classPathNames.length; i++) {
            urls[i] = bundle.getResource(classPathNames[i]);
            if (urls[i] == null) {
                throw new IllegalArgumentException("SharedLibrary classpath entry not found: '" +  classPathNames[i] + "'");
            }
        }

        // Create classloader
        return new JarFileClassLoader(
                        component.getIdentification().getName(),
                        urls,
                        parents,
                        component.isComponentClassLoaderDelegationSelfFirst(),
                        new String[0],
                        new String[] {"java.", "javax." });
    }

    private ClassLoader getSharedLibraryClassLoader(SharedLibraryList sharedLibraryList) {
        SharedLibrary sl = sharedLibraries.get(sharedLibraryList.getName());
        if (sl != null) {
            return sl.createClassLoader();
        } else {
            throw new IllegalStateException("SharedLibrary not installed: " + sharedLibraryList.getName());
        }
    }

}
