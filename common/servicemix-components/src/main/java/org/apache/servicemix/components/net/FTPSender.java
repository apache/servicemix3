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
import java.io.OutputStream;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.servicemix.components.util.DefaultFileMarshaler;
import org.apache.servicemix.components.util.FileMarshaler;
import org.apache.servicemix.components.util.OutBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component which sends a message to a file using FTP via the
 * <a href="http://jakarta.apache.org/commons/net.html">Jakarta Commons Net</a> library
 * 
 * @version $Revision$
 */
public class FTPSender extends OutBinding {

    private static final Logger logger = LoggerFactory.getLogger(FTPSender.class);

    private FTPClientPool clientPool;
    private FileMarshaler marshaler = new DefaultFileMarshaler();
    private String uniqueFileName = "ServiceMix";
    private boolean overwrite = false;

    // Properties
    //-------------------------------------------------------------------------
    public FTPClientPool getClientPool() {
        return clientPool;
    }

    public void setClientPool(FTPClientPool clientPool) {
        this.clientPool = clientPool;
    }

    public FileMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(FileMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public String getUniqueFileName() {
        return uniqueFileName;
    }

    /**
     * Sets the name used to make a unique name if no file name is available on the message.
     *
     * @param uniqueFileName the new value of the unique name to use for generating unique names
     */
    public void setUniqueFileName(String uniqueFileName) {
        this.uniqueFileName = uniqueFileName;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        if (clientPool == null) {
            throw new IllegalArgumentException("You must initialise the clientPool property");
        }
        super.init();
    }

    protected void process(MessageExchange exchange, NormalizedMessage message) throws Exception {
        FTPClient client = null;
        OutputStream out = null;
        try {
            client = (FTPClient) getClientPool().borrowClient();

            String name = marshaler.getOutputName(exchange, message);
            if (name == null) {
                if (uniqueFileName != null) {
                    out = client.storeUniqueFileStream(uniqueFileName);
                }
                else {
                    out = client.storeUniqueFileStream();
                }
            }
            else {
                out = client.storeFileStream(name);
                if (out == null) {
                    // lets try overwrite the previous file?
                    if (overwrite) {
                        client.deleteFile(name);
                    }
                    out = client.storeFileStream(name);
                }
            }
            if (out == null) {
                throw new IOException("No output stream available for output name: " + name + ". Maybe the file already exists?");
            }
            marshaler.writeMessage(exchange, message, out, name);
            done(exchange);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    logger.error("Caught exception while closing stream on error: ", e);
                }
            }
            client.completePendingCommand();
            returnClient(client);
        }
    }

    protected void returnClient(SocketClient client) {
        if (client != null) {
            try {
                getClientPool().returnClient(client);
            }
            catch (Exception e) {
                logger.error("Failed to return client to pool: ", e);
            }
        }
    }

}
