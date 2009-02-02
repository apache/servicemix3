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
package org.servicemix.jboss.deployment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.AutoDeploymentService.ArchiveEntry;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * This is the deployer that handles picking up and deploying a JBI package out
 * to the ServiceMix container.
 *
 * @author <a href="mailto:philip.dodds@unity-systems.com">Philip Dodds</a>
 *
 * @jmx.mbean name="jboss.system:service=ServiceMixJBIContainer"
 *            extends="org.jboss.system.ServiceMBean"
 *
 */
public class JBIService extends ServiceMBeanSupport implements JBIServiceMBean {
    
    private JBIContainer jbiContainer = new JBIContainer();
    
    private String transactionManager = "java:/TransactionManager";
    
    private Map containerMap = new HashMap();
    
    private Map archiveMap = new HashMap();
    
        /*
         * (non-Javadoc)
         *
         * @see org.jboss.system.ServiceMBeanSupport#createService()
         */
    public void createService() throws Exception {
        super.create();
    }
    
    /**
     * Helper method that performs recursive deletes on a directory
     *
     * @param dir
     *            The directory to delete
     * @return True, if successful
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        
        // The directory is now empty so delete it
        return dir.delete();
        
    }
    
        /*
         * (non-Javadoc)
         *
         * @see org.jboss.system.ServiceMBeanSupport#destroyService()
         */
    public void destroyService() {
        log.info("Destroying ServiceMixJBIContainer");
        super.destroy();
    }
    
    /**
     * Gets the location of the transaction manager to use
     *
     * @return The transaction manager
     *
     * @jmx.managed-attribute
     */
    public String getTransactionManager() {
        return transactionManager;
    }
    
    /**
     * Request the JBI Container install the given archive
     *
     * @param archive
     *            The name of the archive to install
     *
     * @jmx.managed-operation
     */
    public void installArchive(String archive) {
        try {
            ArchiveEntry entry = jbiContainer.getAutoDeploymentService().updateExternalArchive(archive, true);
            archiveMap.put(archive, entry);
        } catch (DeploymentException e) {
            throw new RuntimeException(
                    "ServiceMix JBIContainer unable to install archive ["
                    + archive + "]", e);
        }
    }
    
    /**
     * Request the JBI Container uninstall the given archive
     *
     * @param archive
     *            The name of the archive to install
     *
     * @jmx.managed-operation
     */
    public void uninstallArchive(String archive) {
        try {
            ArchiveEntry entry = (ArchiveEntry) archiveMap.get(archive);
            if (entry == null) {
                throw new DeploymentException("No service assembly " + archive + " registered!");
            }
            jbiContainer.getAutoDeploymentService().removeArchive(entry);
        } catch (DeploymentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Request the JBI Container install the given ServiceMix XML
     *
     * @param archive
     *            The name of the archive to install
     *
     * @jmx.managed-operation
     */
    public void installServiceMixXml(String archive) {
        // Ok, we are going to use the spring infrastructure
        // to create a container
        log
                .info("Creating ServiceMix JBI installer from XML [" + archive
                + "]");
        containerMap.put(archive, new ClassPathXmlApplicationContext(archive));
    }
    
    /**
     * Request the JBI Container uninstall the given ServiceMix XML
     *
     * @param archive
     *            The name of the archive to install
     *
     * @jmx.managed-operation
     */
    public void uninstallServiceMixXml(String archive) {
        // Ok, we are going to use the spring infrastructure
        // to create a container
        log.info("Destroying ServiceMix JBI installer from XML [" + archive
                + "]");
        ClassPathXmlApplicationContext context = (ClassPathXmlApplicationContext) containerMap
                .get(archive);
        if (context != null)
            context.destroy();
        else
            log.warn("Unable to find deployed JBI container for XML ["
                    + archive + "]");
    }
    
    /**
     * Sets the location of the transaction manager to use
     *
     * @param transactionManager
     *
     * @jmx.managed-attribute
     */
    public void setTransactionManager(String transactionManager) {
        this.transactionManager = transactionManager;
    }
    
        /*
         * (non-Javadoc)
         *
         * @see org.jboss.system.ServiceMBeanSupport#startService()
         */
    public void startService() throws Exception {
        
        jbiContainer = new JBIContainer();
        jbiContainer.setCreateMBeanServer(false);
        jbiContainer.setMonitorInstallationDirectory(false);
        jbiContainer.setMBeanServer(getServer());
        jbiContainer.setCreateJmxConnector(false);
        jbiContainer.setUseShutdownHook(false);
        
        // TODO Keeping the service mix configuration directory in place is a
        // problem?
        File rootDir = new File(System.getProperty("jboss.server.data.dir")
        + "/ServiceMix");
        log.debug("Checking whether ServiceMix root directory exists ["
                + rootDir.getAbsolutePath() + "] exists[" + rootDir.exists()
                + "]");
        
        if (rootDir.exists() && !deleteDir(rootDir)) {
            throw new Exception(
                    "Unable to delete the ServiceMix root directory at start-up ["
                    + rootDir.getAbsolutePath()
                    + "], check permissions.");
        }
        if (!rootDir.mkdir()) {
            throw new Exception(
                    "Unable to create the ServiceMix root directory at start-up ["
                    + rootDir.getAbsolutePath()
                    + "], check permissions.");
        }
        jbiContainer.setRootDir(rootDir.getAbsolutePath());
        
        // Lets get the transaction manager from JNDI
        InitialContext initCtxt = new InitialContext();
        Object transactionManager = initCtxt.lookup(this.transactionManager);
        jbiContainer.setTransactionManager(transactionManager);
        
        jbiContainer.init();
        jbiContainer.start();
        super.start();
    }
    
        /*
         * (non-Javadoc)
         *
         * @see org.jboss.system.ServiceMBeanSupport#stopService()
         */
    public void stopService() {
        try {
            jbiContainer.stop();
        } catch (JBIException e) {
            log.error("Unable to stop ServiceMixJBIContainer ["
                    + e.getMessage() + "]");
        }
        super.stop();
    }
    
}
