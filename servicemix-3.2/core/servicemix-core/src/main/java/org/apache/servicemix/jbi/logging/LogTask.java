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
import java.net.URLConnection;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class LogTask extends TimerTask {

    private static final Logger LOG = Logger.getLogger(LogTask.class);

    private URL url;

    private long lastConfigured = -1;

    public LogTask(URL url) {
        this.url = url;
    }

    public void run() {
        reconfigure();
    }

    /**
     * reconfigure the log4j system if something has changed
     * 
     * TODO might be good to check if the content type is text so that you can
     * also can do the same with log4j.properties
     */
    public void reconfigure() {
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            long lastModified = conn.getLastModified();
            boolean xml = "application/xml".equals(conn.getContentType());
            conn.getInputStream().close();
            if (lastConfigured < lastModified && url != null && xml) {
                DOMConfigurator.configure(url);
                lastConfigured = System.currentTimeMillis();
                LOG.info("Logging system reconfigured using file: " + url.toString());
            }
        } catch (Exception ex) {
            LOG.error(ex);
        }
    }
}
