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

package org.apache.servicemix.jbi.container;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.framework.LocalComponentConnector;
import org.apache.servicemix.jbi.messaging.MessagingStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
    private PrintWriter statsWriter;
    private LocalComponentConnector localConnector;

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
    public LocalComponentConnector getLocalConnector() {
        return localConnector;
    }

    /**
     * @param localConnector The localConnector to set.
     */
    public void setLocalConnector(LocalComponentConnector localConnector) {
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
                if (statsWriter == null) {
                    File file = new File(componentRoot, "Stats.csv");
                    FileOutputStream fileOut = new FileOutputStream(file);
                    statsWriter = new PrintWriter(fileOut, true);
                    statsWriter.println(localConnector.getComponentNameSpace().getName() + ":");
                    statsWriter.println("inboundExchanges,inboundExchangeRate,outboundExchanges,outboundExchangeRate");
                }
                MessagingStats stats = localConnector.getDeliveryChannel().getMessagingStats();
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
}
