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
package org.apache.servicemix.console;

import org.apache.servicemix.jbi.audit.AuditorMBean;
import org.apache.servicemix.jbi.audit.jdbc.JdbcAuditor;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.DeploymentService;
import org.apache.servicemix.jbi.framework.InstallationService;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.ManagementContextMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jbi.management.DeploymentServiceMBean;
import javax.jbi.management.InstallationServiceMBean;
import javax.jbi.management.LifeCycleMBean;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public abstract class ServiceMixPortlet extends GenericPortlet {

    protected static final transient Logger LOGGER = LoggerFactory.getLogger(ServiceMixPortlet.class);
    
    protected PortletRequestDispatcher normalView;
    protected PortletRequestDispatcher helpView;
    protected PortletRequestDispatcher errorView;

    private JMXConnector jmxConnector;
    private String namingHost = "localhost";
    private String containerName = JBIContainer.DEFAULT_NAME;
    private String jmxDomainName = ManagementContext.DEFAULT_DOMAIN;
    private String jmxUrl;
    private int namingPort = ManagementContext.DEFAULT_CONNECTOR_PORT;
    private String jndiPath = ManagementContext.DEFAULT_CONNECTOR_PATH;
    private String username;
    private String password;

    /**
     * Get the JMXServiceURL - built from the protocol used and host names
     * @return the url
     */
    public JMXServiceURL getServiceURL() {
        JMXServiceURL url = null;
        if (url == null) {
            try {
                String jmxUrl = this.jmxUrl;
                if (jmxUrl == null) {
                    jmxUrl = "service:jmx:rmi:///jndi/rmi://" + namingHost + ":" + namingPort + jndiPath;
                }
                url = new JMXServiceURL(jmxUrl);
            }
            catch (MalformedURLException e) {
                LOGGER.error("error creating serviceURL: ", e);
            }
        }
        return url;
    }
    
    /**
     * Get a JMXConnector from a url
     * @param url
     * @return the JMXConnector
     * @throws IOException
     */
    public JMXConnector getJMXConnector (JMXServiceURL url) throws IOException {
        LOGGER.info("Connecting to JBI Container at: {}", url);
        String[] credentials = new String[] { username, password };
        Map environment = new HashMap();
        environment.put(JMXConnector.CREDENTIALS, credentials);
        return JMXConnectorFactory.connect(url, environment);
    }
    
    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        LOGGER.debug("doHelp");
        helpView.include(renderRequest, renderResponse);
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        LOGGER.debug("doView");
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        try {
            // Retrieve the jmx connector
            if (this.jmxConnector == null) {
                this.jmxConnector = getJMXConnector(getServiceURL());
            }
            renderView(renderRequest, renderResponse);
        } catch (PortletException e) {
            LOGGER.error("Error rendering portlet", e);
            closeConnector();
            throw e;
        } catch (IOException e) {
            LOGGER.error("Error rendering portlet", e);
            closeConnector();
            throw e;
        } catch (Exception e) {
            try {
                LOGGER.debug("Error rendering portlet", e);
                renderRequest.setAttribute("exception", e);
                errorView.include(renderRequest, renderResponse);
            } finally {
                closeConnector();
            }
            //LOGGER.error("Error rendering portlet", e);
            //throw new PortletException("Error rendering portlet", e);
        }
    }
    
    protected void renderView(RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {
        // Fill request
        fillViewRequest(renderRequest);
        // Render view
        normalView.include(renderRequest, renderResponse);
    }
    
    /**
     * Get a servicemix internal system management instance, from it's class name
     * @param systemClass
     * @return the object name
     */
    protected  ObjectName getObjectName (Class systemClass){
        return ManagementContext.getSystemObjectName(jmxDomainName, getContainerName(), systemClass);
    }
    
    
    protected void fillViewRequest(RenderRequest request) throws Exception {
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        LOGGER.debug("init");
        super.init(portletConfig);
        PortletContext pc = portletConfig.getPortletContext();
        normalView = pc.getRequestDispatcher("/WEB-INF/view/" + getPortletName() + "/view.jsp");
        helpView = pc.getRequestDispatcher("/WEB-INF/view/" + getPortletName() + "/help.jsp");
        errorView = pc.getRequestDispatcher("/WEB-INF/view/error.jsp");

        jmxUrl = getConfigString("servicemixJmxUrl", jmxUrl);
        username = getConfigString("servicemixJmxUsername", username);
        password = getConfigString("servicemixJmxPassword", password);
        containerName = getConfigString("servicemixContainerName", containerName);
    }
    
    protected String getConfigString(String name, String defValue) {
        ServletContext ctx = ContextLoaderListener.getServletContext();
        if (ctx != null) {
            String v = ctx.getInitParameter(name);
            if (v != null) {
                return v;
            }
        }
        return defValue;
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        LOGGER.debug("processAction: {}", actionRequest);
        try {
            // Retrieve the jmx connector
            if (this.jmxConnector == null) {
                this.jmxConnector = getJMXConnector(getServiceURL());
            }
            // Fill request
            doProcessAction(actionRequest, actionResponse);
        } catch (PortletException e) {
            LOGGER.error("Error processing action", e);
            closeConnector();
            throw e;
        } catch (IOException e) {
            LOGGER.error("Error processing action", e);
            closeConnector();
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error processing action", e);
            closeConnector();
            throw new PortletException("Error processing action", e);
        }
    }

    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
    }

    public void destroy() {
        closeConnector();
        super.destroy();
    }
    
    protected void closeConnector() {
        if (this.jmxConnector != null){
            try {
                jmxConnector.close();
            } catch (Exception e) {
                LOGGER.warn("caught an error closing the jmxConnector", e);
            } finally {
                jmxConnector = null;
            }
        }
    }
    
    /**
     * Get the InstallationServiceMBean
     * @return the installation service MBean
     * @throws IOException
     */
    public InstallationServiceMBean getInstallationService() throws IOException {
        ObjectName objectName = getObjectName(InstallationService.class);
        return (InstallationServiceMBean) getProxy(objectName, InstallationServiceMBean.class);
    }
    
    /**
     * Get the DeploymentServiceMBean 
     * @return the deployment service mbean
     * @throws IOException
     */
    public DeploymentServiceMBean getDeploymentService() throws IOException {
        ObjectName objectName = getObjectName(DeploymentService.class);
        return (DeploymentServiceMBean) getProxy(objectName, DeploymentServiceMBean.class);
    }
    
    
    /**
     * Get the ManagementContextMBean 
     * @return the management service mbean
     * @throws IOException
     */
    public ManagementContextMBean getManagementContext() throws IOException {
        ObjectName objectName = getObjectName(ManagementContext.class);
        return (ManagementContextMBean) getProxy(objectName, ManagementContextMBean.class);
    }
    
    public AuditorMBean getJdbcAuditor() throws IOException {
        ObjectName objectName = getObjectName(JdbcAuditor.class);
        return (AuditorMBean) getProxy(objectName, AuditorMBean.class);
    }
    
    public LifeCycleMBean getJBIContainer() throws IOException {
        ObjectName objectName = ManagementContext.getContainerObjectName(jmxDomainName, containerName);
        return (LifeCycleMBean) getProxy(objectName, LifeCycleMBean.class);
    }
    
    public Object getProxy(ObjectName name, Class type) throws IOException {
        return MBeanServerInvocationHandler.newProxyInstance(getServerConnection(), name, type, true);
    }
    
    public MBeanServerConnection getServerConnection() throws IOException {
        return jmxConnector.getMBeanServerConnection();
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    
    /**
     * @return the password
     */
    protected String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    protected void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the username
     */
    protected String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    protected void setUsername(String username) {
        this.username = username;
    }

}
