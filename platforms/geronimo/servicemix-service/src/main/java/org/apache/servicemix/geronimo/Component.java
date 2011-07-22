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
package org.apache.servicemix.geronimo;

import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Component implements GBeanLifecycle {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Component.class);

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

    public Component(String name, String description, String type, String className, Container container,
                    URL configurationBaseUrl, ClassLoader classLoader) throws Exception {
        this.name = name;
        this.description = description;
        this.type = type;
        this.className = className;
        this.container = container;
        // TODO is there a simpler way to do this?
        if (configurationBaseUrl.getProtocol().equalsIgnoreCase("file")) {
            this.rootDir = new URI("file", configurationBaseUrl.getPath(), null);
        } else {
            this.rootDir = URI.create(configurationBaseUrl.toString());
        }
        this.installDir = rootDir.resolve("install/");
        this.workDir = rootDir.resolve("workspace/");
        this.classLoader = classLoader;
        LOGGER.debug("Created JBI component: {}", name);
    }

    public void doStart() throws Exception {
        LOGGER.debug("doStart called for JBI component: {}", name);
        try {
            component = (javax.jbi.component.Component) classLoader.loadClass(className).newInstance();
            container.register(this);
        } catch (ClassNotFoundException e) {
            LOGGER.error(classLoader.toString());
        }
        LOGGER.info("Started servicemix JBI component: {}", name);
    }

    public void doStop() throws Exception {
        LOGGER.debug("doStop called for JBI component: {}", name);
        container.unregister(this);
        component = null;
    }

    public void doFail() {
        LOGGER.debug("doFail called for JBI component: {}", name);
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
        infoFactory.setConstructor(new String[] { "name", "description", "type", "className", "container",
                        "configurationBaseUrl", "classLoader" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
