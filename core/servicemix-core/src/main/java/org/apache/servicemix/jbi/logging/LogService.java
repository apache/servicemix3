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
package org.apache.servicemix.jbi.logging;

import java.net.URL;
import java.util.Timer;

import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.log4j.Logger;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @org.apache.xbean.XBean element="logService"
 * 
 * TODO add methods to change one or more specific LogLevels at runtime
 */
public class LogService extends BaseSystemService implements InitializingBean, LogServiceMBean {

    private static final String DEFAULT_LOG_FILE_NAME = "log4j.xml";

    private static final Logger LOG = Logger.getLogger(LogService.class);

    private boolean autoStart = true;

    private boolean initialized;

    private int refreshPeriod = 60; // 60sec

    private URL configFileUrl;

    private String configUrl = "file:conf/log4j.xml";

    private LogTask logTask;

    // timer in daemon mode
    private Timer timer;

    public void afterPropertiesSet() throws Exception {
        if (this.container == null) {
            throw new IllegalArgumentException("container should not be null");
        }
        init(getContainer());
        if (autoStart) {
            start();
        }
    }

    public JBIContainer getContainer() {
        return container;
    }

    public void setContainer(JBIContainer container) {
        this.container = container;
    }

    public String getDescription() {
        return "Log4j Service which periodicaly scan the config file";
    }

    /**
     * 
     * @param seconds
     *            Refresh period for the log4j system.
     */
    public void setRefreshPeriod(int seconds) {
        this.refreshPeriod = seconds;
        try {
            if (isStarted()) {
                stop();
                start();
            }
        } catch (JBIException ex) {
            LOG.error("Error occured!", ex);
        }
    }

    /**
     * 
     * @return returns the time in seconds for the refresh period
     */
    public int getRefreshPeriod() {
        return this.refreshPeriod;
    }

    /**
     * set new location for log4j config
     * 
     * @param url
     *            Location for log4j config file example: file:conf/log4j.xml
     */
    public void setConfigUrl(String url) {
        this.configUrl = url;
        try {
            if (isStarted()) {
                stop();
                start();
            }
        } catch (JBIException ex) {
            LOG.error("Error occured!", ex);
        }

    }

    public String getConfigUrl() {
        return this.configUrl;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean getAutoStart() {
        return this.autoStart;
    }

    /**
     * reconfigure the log4j system if something has changed in the config file
     */
    public void reconfigureLogSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("try to reconfigure the log4j system");
        }
        if (logTask != null) {
            logTask.reconfigure();
        }
    }

    protected Class getServiceMBean() {
        return LogServiceMBean.class;
    }

    public void start() throws JBIException {
        setUp();
        super.start();
    }

    public void stop() throws JBIException {
        if (logTask != null) {
            logTask.cancel();
            logTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        initialized = false;
        super.stop();
    }

    public void setUp() throws JBIException {
        if (!initialized) {
            configFileUrl = locateLoggingConfig();

            if (configFileUrl != null) {
                // daemon mode
                timer = new Timer(true);
                logTask = new LogTask(configFileUrl);
                logTask.run();
                timer.schedule(logTask, 1000 * refreshPeriod, 1000 * refreshPeriod);
                initialized = true;
            }
        }
    }

    /**
     * Grab the log4j.xml from the CLASSPATH
     * 
     * @return URL of the log4j.xml file
     */
    private URL locateLoggingConfig() {
        URL log4jConfigUrl = ClassLoader.getSystemResource(DEFAULT_LOG_FILE_NAME);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Located logging configuration: " + log4jConfigUrl.toString());
        }

        return log4jConfigUrl;
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "reconfigureLogSystem", 0, "Reconfigure the log4j system");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "configUrl", "the url for the log4j.xml config file");
        helper.addAttribute(getObjectToManage(), "refreshPeriod", "schedule time for scanning the log4j config file");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }
}
