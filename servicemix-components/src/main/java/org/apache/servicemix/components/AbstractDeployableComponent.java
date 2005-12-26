/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.apache.servicemix.components;

import java.util.Map;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.LifeCycleMBean;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDeployableComponent extends AbstractComponent implements ServiceUnitManager {

    protected Map serviceUnits = new ConcurrentHashMap();
    
    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getServiceUnitManager()
     */
    public final ServiceUnitManager getServiceUnitManager() {
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ServiceUnitManager#deploy(java.lang.String, java.lang.String)
     */
    public synchronized String deploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Deploying service unit");
            }
            if (serviceUnitName == null || serviceUnitName.length() == 0) {
                throw new IllegalArgumentException("serviceUnitName should be non null and non empty");
            }
            if (this.serviceUnits.get(serviceUnitName) != null) {
                throw new DeploymentException(createFailureMessage("deploy", "Service Unit '" + serviceUnitName + "' is already deployed"));
            }
            ServiceUnit su = doDeploy(serviceUnitName, serviceUnitRootPath);
            this.serviceUnits.put(serviceUnitName, su);
            if (logger.isDebugEnabled()) {
                logger.debug("Service unit deployed");
            }
            return createSuccessMessage("deploy");
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(createFailureMessage("deploy", e));
        }
    }
    
    protected abstract ServiceUnit doDeploy(String serviceUnitName, String serviceUnitRootPath) throws Exception;

    /* (non-Javadoc)
     * @see javax.jbi.component.ServiceUnitManager#init(java.lang.String, java.lang.String)
     */
    public synchronized void init(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing service unit");
            }
            if (serviceUnitName == null || serviceUnitName.length() == 0) {
                throw new IllegalArgumentException("serviceUnitName should be non null and non empty");
            }
            if (this.serviceUnits.get(serviceUnitName) == null) {
                throw new DeploymentException(createFailureMessage("deploy", "Service Unit '" + serviceUnitName + "' is not deployed"));
            }
            doInit(serviceUnitName, serviceUnitRootPath);
            if (logger.isDebugEnabled()) {
                logger.debug("Service unit initialized");
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(createFailureMessage("init", e));
        }
    }

    protected void doInit(String serviceUnitName, String serviceUnitRootPath) throws Exception {
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ServiceUnitManager#start(java.lang.String)
     */
    public synchronized void start(String serviceUnitName) throws DeploymentException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting service unit");
            }
            if (serviceUnitName == null || serviceUnitName.length() == 0) {
                throw new IllegalArgumentException("serviceUnitName should be non null and non empty");
            }
            ServiceUnit su = (ServiceUnit) this.serviceUnits.get(serviceUnitName);
            if (su == null) {
                throw new DeploymentException(createFailureMessage("deploy", "Service Unit '" + serviceUnitName + "' is not deployed"));
            }
            if (!LifeCycleMBean.STOPPED.equals(su.getCurrentState()) &&
                !LifeCycleMBean.SHUTDOWN.equals(su.getCurrentState())) {
                throw new DeploymentException("ServiceUnit should be in a SHUTDOWN or STOPPED state");
            }
            su.start();
            if (logger.isDebugEnabled()) {
                logger.debug("Service unit started");
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(createFailureMessage("start", e));
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ServiceUnitManager#stop(java.lang.String)
     */
    public synchronized void stop(String serviceUnitName) throws DeploymentException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping service unit");
            }
            if (serviceUnitName == null || serviceUnitName.length() == 0) {
                throw new IllegalArgumentException("serviceUnitName should be non null and non empty");
            }
            ServiceUnit su = (ServiceUnit) this.serviceUnits.get(serviceUnitName);
            if (su == null) {
                throw new DeploymentException(createFailureMessage("deploy", "Service Unit '" + serviceUnitName + "' is not deployed"));
            }
            if (!LifeCycleMBean.RUNNING.equals(su.getCurrentState())) {
                throw new DeploymentException("ServiceUnit should be in a SHUTDOWN state");
            }
            su.stop();
            if (logger.isDebugEnabled()) {
                logger.debug("Service unit stopped");
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(createFailureMessage("stop", e));
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ServiceUnitManager#shutDown(java.lang.String)
     */
    public synchronized void shutDown(String serviceUnitName) throws DeploymentException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Shutting down service unit");
            }
            if (serviceUnitName == null || serviceUnitName.length() == 0) {
                throw new IllegalArgumentException("serviceUnitName should be non null and non empty");
            }
            ServiceUnit su = (ServiceUnit) this.serviceUnits.get(serviceUnitName);
            if (su == null) {
                throw new DeploymentException(createFailureMessage("deploy", "Service Unit '" + serviceUnitName + "' is not deployed"));
            }
            su.shutDown();
            if (logger.isDebugEnabled()) {
                logger.debug("Service unit shut down");
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(createFailureMessage("shutDown", e));
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ServiceUnitManager#undeploy(java.lang.String, java.lang.String)
     */
    public synchronized String undeploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Undeploying service unit");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Shutting down service unit");
            }
            if (serviceUnitName == null || serviceUnitName.length() == 0) {
                throw new IllegalArgumentException("serviceUnitName should be non null and non empty");
            }
            ServiceUnit su = (ServiceUnit) this.serviceUnits.get(serviceUnitName);
            if (su == null) {
                throw new DeploymentException(createFailureMessage("deploy", "Service Unit '" + serviceUnitName + "' is not deployed"));
            }
            if (!LifeCycleMBean.SHUTDOWN.equals(su.getCurrentState())) {
                throw new DeploymentException("ServiceUnit should be in a SHUTDOWN state");
            }
            doUndeploy(su);
            this.serviceUnits.remove(serviceUnitName);
            if (logger.isDebugEnabled()) {
                logger.debug("Service unit undeployed");
            }
            return createSuccessMessage("undeploy");
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(createFailureMessage("undeploy", e));
        }
    }

    protected void doUndeploy(ServiceUnit su) throws Exception {
    }

    protected String createSuccessMessage(String task) {
        ManagementMessageHelper.Message msg = new ManagementMessageHelper.Message();
        msg.setComponent(context.getComponentName());
        msg.setTask(task);
        msg.setResult("SUCCESS");
        return ManagementMessageHelper.createComponentMessage(msg);
    }
    
    protected String createFailureMessage(String task, Exception e) {
        ManagementMessageHelper.Message msg = new ManagementMessageHelper.Message();
        msg.setComponent(context.getComponentName());
        msg.setTask(task);
        msg.setResult("FAILED");
        msg.setType("ERROR");
        msg.setException(e);
        return ManagementMessageHelper.createComponentMessage(msg);
    }
    
    protected String createFailureMessage(String task, String info) {
        ManagementMessageHelper.Message msg = new ManagementMessageHelper.Message();
        msg.setComponent(context.getComponentName());
        msg.setTask(task);
        msg.setResult("FAILED");
        msg.setType("ERROR");
        msg.setMessage(info);
        return ManagementMessageHelper.createComponentMessage(msg);
    }
    
}
