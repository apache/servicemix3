/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.gbean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class ServiceMixDeployment implements GBeanLifecycle {

    private Log log = LogFactory.getLog(getClass().getName());
    
    private String name;
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ServiceMix Deployment", ServiceMixDeployment.class, "JBIDeployment");
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.setConstructor(new String[]{"name"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
    public ServiceMixDeployment(String name) {
        this.name = name;
    }

    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
        log.info("Starting JBI deployment: " + name );
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        log.info("Stopping JBI deployment: " + name);
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        log.info("Failing JBI deployment: " + name);
    }

}
