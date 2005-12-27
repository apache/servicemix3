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
package org.apache.servicemix.jbi.framework;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.jbi.management.DeploymentServiceMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.util.XmlPersistenceSupport;
/**
 * ComponentConnector is used internally for message routing
 * 
 * @version $Revision$
 */
class ServiceAssemblyLifeCycle{
    private static final Log log=LogFactory.getLog(ServiceAssemblyLifeCycle.class);
    static final String UNKOWN="Unknown";
    static final String STARTED=DeploymentServiceMBean.STARTED;
    static final String SHUTDOWN=DeploymentServiceMBean.SHUTDOWN;
    static final String STOPPED=DeploymentServiceMBean.STOPPED;
    private ServiceAssembly serviceAssembly;
    private String currentState=SHUTDOWN;
    private File stateFile;

    /**
     * Construct a LifeCycle
     * 
     * @param sa
     * @param stateFile
     */
    ServiceAssemblyLifeCycle(ServiceAssembly sa,File stateFile){
        this.serviceAssembly=sa;
        this.stateFile=stateFile;
    }

    /**
     * Start the item.
     */
    void start(){
        currentState=STARTED;
    }

    /**
     * Stop the item. This suspends current messaging activities.
     */
    void stop(){
        currentState=STOPPED;
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     */
    void shutDown(){
        currentState=SHUTDOWN;
    }

    /**
     * @return the currentState as a String
     */
    public String getCurrentState(){
        return currentState;
    }

    boolean isShutDown(){
        return currentState==null||currentState.equals(UNKOWN)||currentState.equals(SHUTDOWN);
    }

    boolean isStopped(){
        return currentState!=null&&currentState.equals(STOPPED);
    }

    boolean isStarted(){
        return currentState!=null&&currentState.equals(STARTED);
    }

    /**
     * @return the name of the ServiceAssembly
     */
    String getName(){
        return serviceAssembly.getIdentification().getName();
    }

    /**
     * @return the ServiceAssembly
     */
    ServiceAssembly getServiceAssembly(){
        return serviceAssembly;
    }

    /**
     * @return string representation of this
     */
    public String toString(){
        return getName()+" ServiceAssembly lifecycle: "+getCurrentState();
    }

    /**
     * write the current running state of the Component to disk
     */
    void writeRunningState(){
        try{
            String currentState = getCurrentState();
            Properties props = new Properties();
            props.setProperty("state", currentState);
            XmlPersistenceSupport.write(stateFile, props);
        }catch(IOException e){
            log.error("Failed to write current running state for ServiceAssembly: "+getName(),e);
        }
    }

    /**
     * get the current running state from disk
     */
    void getRunningStateFromStore(){
        try{
            if(stateFile.exists()){
                Properties props = (Properties) XmlPersistenceSupport.read(stateFile);
                currentState = props.getProperty("state",SHUTDOWN);
            }
        }catch(Exception e){
            log.error("Failed to read current running state for ServiceAssembly: "+getName(),e);
        }
    }
}
