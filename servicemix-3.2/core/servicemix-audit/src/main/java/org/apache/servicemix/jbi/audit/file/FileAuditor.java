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
package org.apache.servicemix.jbi.audit.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.MessageUtil;

/**
 * Simple implementation of a ServiceMix auditor that stores messages in files in a directory.
 * 
 * Shows usage of {@link TeeInputStream} for auditing {@link StreamSource} message content 
 * 
 * @org.apache.xbean.XBean element="fileAuditor" description="The Auditor of message exchanges to a directory"
 * 
 * @author Gert Vanthienen (gertv)
 * @since 3.2
 */
public class FileAuditor extends AbstractAuditor {

    private static final Log LOG = LogFactory.getLog(FileAuditor.class);
    private File directory;

    /**
     * The directory used for storing the audited messages
     * 
     * @param directory
     *            the directory
     */
    public void setDirectory(File directory) {
        if (!directory.exists()) {
            LOG.info("Creating directory " + directory);
            directory.mkdirs();
        }
        this.directory = directory;
    }

    public void exchangeSent(ExchangeEvent event) {
        try {
            MessageExchange exchange = event.getExchange();
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                OutputStream os = getOutputStream(exchange);
                NormalizedMessage in = exchange.getMessage("in");
                if (StreamSource.class.isAssignableFrom(in.getContent().getClass())) {
                    StreamSource original = (StreamSource) exchange.getMessage("in").getContent();
                    TeeInputStream tis = new TeeInputStream(original.getInputStream(), os);
                    exchange.getMessage("in").setContent(new StreamSource(tis));
                } else {
                    MessageUtil.enableContentRereadability(in);
                    SourceTransformer transformer = new SourceTransformer();
                    transformer.toResult(in.getContent(), new StreamResult(os));
                }
            }
        } catch (IOException e) {
            LOG.error(String.format("Error occurred while storing message %s", event.getExchange().getExchangeId()), e);
        } catch (TransformerException e) {
            LOG.error(String.format("Error occurred while storing message %s", event.getExchange().getExchangeId()), e);
        } catch (MessagingException e) {
            LOG.error(String.format("Error occurred while storing message %s", event.getExchange().getExchangeId()), e);
        }
    }

    private OutputStream getOutputStream(MessageExchange exchange) throws FileNotFoundException {
        String name = getNameForId(exchange.getExchangeId());
        return new BufferedOutputStream(new FileOutputStream(new File(directory, name)));
    }

    private String getNameForId(String id) {
        return id.replaceAll("[:\\.]", "_");
    }

    @Override
    public int deleteExchangesByIds(String[] ids) throws AuditorException {
        int count = 0;
        for (String id : ids) {
            File file = new File(directory, getNameForId(id));
            if (file.delete()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getExchangeCount() throws AuditorException {
        return directory.listFiles().length;
    }

    @Override
    public String[] getExchangeIdsByRange(int fromIndex, int toIndex) throws AuditorException {
        throw new AuditorException("getExchangeIdsByRange currently unsupported by FileAuditor");
    }

    @Override
    public MessageExchange[] getExchangesByIds(String[] ids) throws AuditorException {
        throw new AuditorException("getExchangeByIds currently unsupported by FileAuditor");
    }

    public String getDescription() {
        return "A file-based auditor implementation: archives files to a specified target directory";
    }
}
