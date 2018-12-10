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
package org.apache.servicemix.jbi.container;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.messaging.MessagingStats;

/**
 * Holder for environment infomation
 * 
 * @version $Revision$
 */
public class ComponentEnvironment {
    
    private static final Log log = LogFactory.getLog(ComponentEnvironment.class);
    
    private File installRoot;
    private File workspaceRoot;
    private File componentRoot;
    private File stateFile;
    private File statsFile;
    private PrintWriter statsWriter;
    private ComponentMBeanImpl localConnector;

    /**
     * @return Returns the installRoot.
     */
    public File getInstallRoot() {
        return installRoot;
    }

    /**
     * @param installRoot The installRoot to set.
     */
    public void setInstallRoot(File installRoot) {
        this.installRoot = installRoot;
    }

    /**
     * @return Returns the workspaceRoot.
     */
    public File getWorkspaceRoot() {
        return workspaceRoot;
    }

    /**
     * @param workspaceRoot The workspaceRoot to set.
     */
    public void setWorkspaceRoot(File workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    /**
     * @return Returns the localConnector.
     */
    public ComponentMBeanImpl getLocalConnector() {
        return localConnector;
    }

    /**
     * @param localConnector The localConnector to set.
     */
    public void setLocalConnector(ComponentMBeanImpl localConnector) {
        this.localConnector = localConnector;
    }

    /**
     * @return Returns the componentRoot.
     */
    public File getComponentRoot() {
        return componentRoot;
    }

    /**
     * @param componentRoot The componentRoot to set.
     */
    public void setComponentRoot(File componentRoot) {
        this.componentRoot = componentRoot;
    }

    /**
     * @return Returns the stateFile.
     */
    public File getStateFile() {
        return stateFile;
    }

    /**
     * @param stateFile The stateFile to set.
     */
    public void setStateFile(File stateFile) {
        this.stateFile = stateFile;
    }
    
    /**
     * close this environment
     */
    public synchronized void close() {
        if (statsWriter != null) {
            statsWriter.close();
        }
    }

    /**
     * dump stats
     */
    public synchronized void dumpStats() {
        if (componentRoot != null && componentRoot.exists()) {
            try {
                if (statsWriter == null && statsFile != null) {
                    FileOutputStream fileOut = new FileOutputStream(statsFile);
                    statsWriter = new PrintWriter(fileOut, true);
                    statsWriter.println(localConnector.getComponentNameSpace().getName() + ":");
                    statsWriter.println("inboundExchanges,inboundExchangeRate,outboundExchanges,outboundExchangeRate");
                }
                MessagingStats stats = localConnector.getMessagingStats();
                long inbound = stats.getInboundExchanges().getCount();
                double inboundRate = stats.getInboundExchangeRate().getAveragePerSecond();
                long outbound = stats.getOutboundExchanges().getCount();
                double outboundRate = stats.getOutboundExchangeRate().getAveragePerSecond();
                statsWriter.println(inbound + "," + inboundRate + "," + outbound + "," + outboundRate);
            }
            catch (IOException e) {
                log.warn("Failed to dump stats", e);
            }
        }
    }

    /**
     * @return Returns the statsFile.
     */
    public File getStatsFile() {
        return statsFile;
    }

    /**
     * @param statsFile The statsFile to set.
     */
    public void setStatsFile(File statsFile) {
        this.statsFile = statsFile;
    }

}
