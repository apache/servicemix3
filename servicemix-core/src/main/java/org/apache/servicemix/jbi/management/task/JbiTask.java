/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package org.apache.servicemix.jbi.management.task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.DeploymentService;
import org.apache.servicemix.jbi.framework.FrameworkInstallationService;
import org.apache.servicemix.jbi.framework.InstallationService;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.ManagementContextMBean;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import javax.jbi.management.AdminServiceMBean;
import javax.jbi.management.DeploymentServiceMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * A bean for connecting to a remote JMX MBeanServer
 * 
 * @version $Revision$
 */
public abstract class JbiTask extends Task {
    private static final Log log = LogFactory.getLog(JbiTask.class);
    private String serverProtocol = "rmi";
    private String host = "localhost";
    private String containerName = JBIContainer.DEFAULT_NAME;
    private String jmxDomainName = ManagementContext.DEFAULT_DOMAIN;
    private int port = 1099;
    private String jndiPath = null;
    private String username;
    private String password;
    private JMXConnector jmxConnector;
    
    
    /**
     * Get the JMXServiceURL - built from the protocol used and host names
     * @return the url
     */
    public JMXServiceURL getServiceURL(){
        JMXServiceURL url = null;
        if (jndiPath == null){
           jndiPath =  "/" + containerName+ "JMX";
        }
        try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" 
                    + host + ":" + port + jndiPath);
        }
        catch (MalformedURLException e) {
            log.error("error creating serviceURL: ",e);
        }
        return url;
    }
    
    /**
     * Get a JMXConnector from a url
     * @param url
     * @return the JMXConnector
     * @throws IOException
     */
    public JMXConnector getJMXConnector (JMXServiceURL url) throws IOException{
        return JMXConnectorFactory.connect(url);
    }
    
    /**
     * initialize the connection
     * @throws BuildException
     */
    public void init() throws BuildException{
        super.init();
        try {
            this.jmxConnector = getJMXConnector(getServiceURL());
        }
        catch (IOException e) {
            log.error("Failed to initialize the JbiTask",e);
            throw new BuildException(e);
        }
    }
    
    
    /**
     * close any internal remote connections
     *
     */
    public void close(){
        if (this.jmxConnector != null){
            try {
                jmxConnector.close();
            }
            catch (IOException e) {
                log.warn("caught an error closing the jmxConnector",e);
            }
        }
    }
    
    
    /**
     * Get a servicemix internal system management instance, from it's class name
     * @param systemClass
     * @return the object name
     */
    protected  ObjectName getObjectName (Class systemClass){
        return ManagementContext.getSystemObjectName(jmxDomainName, containerName, systemClass);
    }
    
        
    /**
     * Get the InstallationServiceMBean
     * @return the installation service MBean
     * @throws IOException
     */
    public FrameworkInstallationService getInstallationService() throws IOException{
        ObjectName objectName = getObjectName(InstallationService.class);
        
        return (FrameworkInstallationService) MBeanServerInvocationHandler.newProxyInstance(jmxConnector.getMBeanServerConnection(), objectName,
                FrameworkInstallationService.class, true);
    }
    
    /**
     * Get the DeploymentServiceMBean 
     * @return the deployment service mbean
     * @throws IOException
     */
    public DeploymentServiceMBean getDeploymentService() throws IOException{
        ObjectName objectName = getObjectName(DeploymentService.class);
        
        return (DeploymentServiceMBean) MBeanServerInvocationHandler.newProxyInstance(jmxConnector.getMBeanServerConnection(), objectName,
                DeploymentServiceMBean.class, true);
    }
    
    
    /**
     * Get the ManagementContextMBean 
     * @return the management service mbean
     * @throws IOException
     */
    public ManagementContextMBean getManagementContext() throws IOException{
        ObjectName objectName = getObjectName(AdminServiceMBean.class);
        
        return (ManagementContextMBean) MBeanServerInvocationHandler.newProxyInstance(jmxConnector.getMBeanServerConnection(), objectName,
                ManagementContextMBean.class, true);
    }
    
    
    /**
     * @return Returns the containerName.
     */
    public String getContainerName() {
        return containerName;
    }
    /**
     * @param containerName The containerName to set.
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    /**
     * @return Returns the jmxDomainName.
     */
    public String getJmxDomainName() {
        return jmxDomainName;
    }
    /**
     * @param jmxDomainName The jmxDomainName to set.
     */
    public void setJmxDomainName(String jmxDomainName) {
        this.jmxDomainName = jmxDomainName;
    }
    /**
     * @return Returns the jndiPath.
     */
    public String getJndiPath() {
        return jndiPath;
    }
    /**
     * @param jndiPath The jndiPath to set.
     */
    public void setJndiPath(String jndiPath) {
        this.jndiPath = jndiPath;
    }
    /**
     * @return Returns the namingHost.
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host The namingHost to set.
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * @return Returns the namingPort.
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port The namingPort to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * @return Returns the serverProtocol.
     */
    public String getServerProtocol() {
        return serverProtocol;
    }
    /**
     * @param serverProtocol The serverProtocol to set.
     */
    public void setServerProtocol(String serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    /**
     * @return Returns the passwd.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The passwd to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}