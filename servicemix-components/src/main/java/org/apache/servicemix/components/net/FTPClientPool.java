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
package org.apache.servicemix.components.net;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.IOException;

/**
 * A pool of FTP clients for
 * the <a href="http://jakarta.apache.org/commons/net.html">Jakarta Commons Net</a> library
 *
 * @version $Revision$
 */
public class FTPClientPool extends SocketClientPoolSupport {

    private String username;
    private String password;
    private boolean binaryMode = true;
    private FTPClientConfig config;

    public boolean validateObject(Object object) {
        FTPClient client = (FTPClient) object;
        try {
            return client.sendNoOp();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to validate client: " + e, e);
        }
    }

    public void activateObject(Object object) throws Exception {
        FTPClient client = (FTPClient) object;
        client.setReaderThread(true);
    }

    public void passivateObject(Object object) throws Exception {
        FTPClient client = (FTPClient) object;
        client.setReaderThread(false);
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBinaryMode() {
        return binaryMode;
    }

    public void setBinaryMode(boolean binaryMode) {
        this.binaryMode = binaryMode;
    }

    public FTPClientConfig getConfig() {
        return config;
    }

    public void setConfig(FTPClientConfig config) {
        this.config = config;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void connect(SocketClient client) throws Exception {
        FTPClient ftp = (FTPClient) client;

        if (config != null) {
            ftp.configure(config);
        }
        super.connect(client);

        int code = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(code)) {
            ftp.disconnect();
            throw new ConnectionRefusedException(code);
        }
        if (!ftp.login(getUsername(), getPassword())) {
            ftp.disconnect();
            throw new ConnectionRefusedException(ftp.getReplyCode());
        }
        if (isBinaryMode()) {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
        }
    }

    protected void disconnect(SocketClient client) throws Exception {
        FTPClient ftp = (FTPClient) client;
        if (ftp.isConnected()) {
            ftp.logout();
        }
        super.disconnect(client);
    }

    protected SocketClient createSocketClient() {
        return new FTPClient();
    }
}
