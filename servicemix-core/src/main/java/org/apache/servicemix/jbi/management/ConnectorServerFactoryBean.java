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
package org.apache.servicemix.jbi.management;

import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;
import org.springframework.jmx.support.MBeanRegistrationSupport;

public class ConnectorServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean{

    private Log log = LogFactory.getLog(ConnectorServerFactoryBean.class);
    private org.springframework.jmx.support.ConnectorServerFactoryBean csfb = new org.springframework.jmx.support.ConnectorServerFactoryBean();
    private String serviceUrl = org.springframework.jmx.support.ConnectorServerFactoryBean.DEFAULT_SERVICE_URL;
    private boolean daemon = false;
    private boolean threaded = false;
    private Map environment;
    private String objectName;
    private int registrationBehavior;
    private MBeanServer server;
    private static final Constants constants = new Constants(MBeanRegistrationSupport.class);
    

    /**
     * @param daemon
     * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setDaemon(boolean)
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * @param environment
     * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setEnvironment(java.util.Properties)
     */
    public void setEnvironment(Properties environment) {
        this.environment = environment;
    }

    /**
     * @param environment
     * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setEnvironmentMap(java.util.Map)
     */
    public void setEnvironmentMap(Map environment) {
        this.environment = environment;
    }

    /**
     * @param objectName
     * @throws MalformedObjectNameException
     * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setObjectName(java.lang.String)
     */
    public void setObjectName(String objectName) throws MalformedObjectNameException {
        this.objectName = objectName;
    }

    /**
     * @param registrationBehavior
     * @see org.springframework.jmx.support.MBeanRegistrationSupport#setRegistrationBehavior(int)
     */
    public void setRegistrationBehavior(int registrationBehavior) {
        this.registrationBehavior = registrationBehavior;
    }

    /**
     * @param registrationBehavior
     * @see org.springframework.jmx.support.MBeanRegistrationSupport#setRegistrationBehaviorName(java.lang.String)
     */
    public void setRegistrationBehaviorName(String registrationBehavior) {
        setRegistrationBehavior(constants.asNumber(registrationBehavior).intValue());
    }

    /**
     * @param server
     * @see org.springframework.jmx.support.MBeanRegistrationSupport#setServer(javax.management.MBeanServer)
     */
    public void setServer(MBeanServer server) {
        this.server = server;
    }

    /**
     * @param serviceUrl
     * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setServiceUrl(java.lang.String)
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * @param threaded
     * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setThreaded(boolean)
     */
    public void setThreaded(boolean threaded) {
        csfb.setThreaded(threaded);
    }

    public Object getObject() throws Exception {
        return csfb.getObject();
    }

    public Class getObjectType() {
        return csfb.getObjectType();
    }

    public boolean isSingleton() {
        return csfb.isSingleton();
    }

    public void afterPropertiesSet() throws Exception {
        csfb = new org.springframework.jmx.support.ConnectorServerFactoryBean();
        csfb.setDaemon(daemon);
        csfb.setThreaded(threaded);
        csfb.setRegistrationBehavior(registrationBehavior);
        csfb.setEnvironmentMap(environment);
        csfb.setObjectName(objectName);
        csfb.setServiceUrl(serviceUrl);
        csfb.setServer(server);
        csfb.afterPropertiesSet();
        log.info("JMX connector available at: " + serviceUrl);
    }

    public void destroy() throws Exception {
        if (csfb != null) {
            try {
                csfb.destroy();
            } finally {
                csfb = null;
            }
        }
    }

}
