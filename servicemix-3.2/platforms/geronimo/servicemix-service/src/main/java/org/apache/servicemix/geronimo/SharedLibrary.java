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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class SharedLibrary implements GBeanLifecycle {

    private static final Log log = LogFactory.getLog(SharedLibrary.class);
    
    private String name;
    private String description;
    private Container container;
    
    public SharedLibrary(String name, String description, Container container) {
        this.name = name;
        this.description = description;
        this.container = container;
    }
    
    public String getName() {
        return this.name;
    }

    public void doStart() throws Exception {
        log.info("doStart called for JBI service assembly: " + name);
    }

    public void doStop() throws Exception {
        log.info("doStop called for JBI service assembly: " + name);
    }

    public void doFail() {
        log.info("doFail called for JBI service assembly: " + name);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("JBISharedLibrary", SharedLibrary.class, "JBISharedLibrary");
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("description", String.class, true);
        infoFactory.addReference("container", Container.class);
        infoFactory.setConstructor(new String[] {"name", "description", "container"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
