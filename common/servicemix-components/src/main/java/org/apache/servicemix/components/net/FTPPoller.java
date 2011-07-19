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
package org.apache.servicemix.components.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.servicemix.components.util.DefaultFileMarshaler;
import org.apache.servicemix.components.util.FileMarshaler;
import org.apache.servicemix.components.util.PollingComponentSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A  component which polls for files to arrive on an FTP server
 * using the <a href="http://jakarta.apache.org/commons/net.html">Jakarta Commons Net</a> library
 * and then sends them into the normalized message service, using a plugable transformer
 * and removes them.
 *
 * @version $Revision$
 */
public class FTPPoller extends PollingComponentSupport {

    private static final Logger logger = LoggerFactory.getLogger(FTPPoller.class);

    private FTPClientPool clientPool;
    private String path;
    private FileMarshaler marshaler = new DefaultFileMarshaler();
    private Set workingSet = new CopyOnWriteArraySet();

    private String getWorkingPath() {
      return path == null ? "." : path;
    }

    public void poll() throws Exception {
        FTPClient ftp = (FTPClient) borrowClient();
        try {
            FTPFile[] files = ftp.listFiles(getWorkingPath());
            for (int i = 0; i < files.length; i++) {
                pollFile(files[i]);
            }
        }
        finally {
            returnClient(ftp);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public FTPClientPool getClientPool() {
        return clientPool;
    }

    public void setClientPool(FTPClientPool clientPool) {
        this.clientPool = clientPool;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(FileMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * The set of FTPFiles that this component is currently working on
     */
    public Set getWorkingSet() {
        return workingSet;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void init() throws JBIException {
        if (clientPool == null) {
            throw new IllegalArgumentException("You must initialise the clientPool property");
        }
        super.init();
    }

    protected void pollFile(final FTPFile aFile) {
        if (workingSet.add(aFile)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduling file " + aFile + " for processing");
            }
            getExecutor().execute(new Runnable() {
                public void run() {
                    try {
                        processFile(aFile);
                    } finally {
                        workingSet.remove(aFile);
                    }
                }
            });
        }
    }

    protected void processFile(FTPFile file) {
        if (file.getName().equals(".") || file.getName().equals("..")) { // TODO: what about other directories?
          return;
        }
        FTPClient client = null;
        try {
            client = (FTPClient) borrowClient();
            processFile(client, file);
            if (!client.deleteFile(getWorkingPath() + file.getName())) {
                throw new IOException("Could not delete file " + file);
            }
        }
        catch (Exception e) {
            logger.error("Failed to process file: " + file + ". Reason: " + e, e);
        }
        finally {
            if (client != null) {
                returnClient(client);
            }
        }
    }

    protected void processFile(FTPClient client, FTPFile file) throws Exception {
        String name = file.getName();
        InputStream in = client.retrieveFileStream(getWorkingPath() + name);
        InOnly exchange = getExchangeFactory().createInOnlyExchange();
        NormalizedMessage message = exchange.createMessage();
        exchange.setInMessage(message);
        marshaler.readMessage(exchange, message, in, name);
        getDeliveryChannel().sendSync(exchange);
        in.close();
        client.completePendingCommand();
    }


    protected SocketClient borrowClient() throws JBIException {
        try {
            return getClientPool().borrowClient();
        }
        catch (Exception e) {
            throw new JBIException(e);
        }
    }

    protected void returnClient(SocketClient client) {
        if (client != null) {
            try {
                getClientPool().returnClient(client);
            }
            catch (Exception e) {
                logger.error("Failed to return client to pool: " + e, e);
            }
        }
    }

}
