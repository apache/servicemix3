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
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.jbi.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Simple implementation of a ServiceMix auditor that stores messages in files in a directory.
 * Shows usage of {@link TeeInputStream} for auditing {@link StreamSource} message content. 
 * 
 * Currently, the file auditor will only store the message body for ACTIVE exchanges.
 * 
 * @org.apache.xbean.XBean element="fileAuditor" description="The Auditor of message exchanges to a directory"
 */
public class FileAuditor extends AbstractAuditor implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileAuditor.class);

    private File directory;
    private FileAuditorStrategy strategy = new FileAuditorStrategyImpl();
    private boolean autostart = true;

    /**
     * The directory used for storing the audited messages
     * 
     * @param directory
     *            the directory
     */
    public void setDirectory(File directory) {
        if (!directory.exists()) {
            LOGGER.info("Creating directory " + directory);
            directory.mkdirs();
        }
        this.directory = directory;
    }

    /**
     * {@inheritDoc}
     */
    public void exchangeSent(ExchangeEvent event) {
        try {
            MessageExchange exchange = event.getExchange();
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                OutputStream os = getOutputStream(exchange);
                writeFileHeader(os, exchange);
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
            LOGGER.error("Error occured while storing message {}", event.getExchange().getExchangeId(), e);
        } catch (TransformerException e) {
            LOGGER.error("Error occurred while storing message {}", event.getExchange().getExchangeId(), e);
        } catch (MessagingException e) {
            LOGGER.error("Error occurred while storing message {}", event.getExchange().getExchangeId(), e);
        }
    }

    private void writeFileHeader(OutputStream os, MessageExchange exchange) {
        MessageExchangeWriter writer = new MessageExchangeWriter(os);
        writer.writeMessageExchange(exchange);
        writer.println(); 
        writer.println("-- Normalized message (in) --");
        writer.writeNormalizedMessage(exchange.getMessage("in"));
        writer.flush();
    }

    /*
     * Get the outputstream for writing the message content
     */
    private OutputStream getOutputStream(MessageExchange exchange) throws FileNotFoundException {
        File file = new File(directory, strategy.getFileName(exchange));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    @Override
    public int deleteExchangesByIds(String[] ids) throws AuditorException {
        throw new AuditorException("deleteExchangesById(s) currently unsupported by FileAuditor");
    }

    @Override
    public int getExchangeCount() throws AuditorException {
        return FileUtil.countFilesInDirectory(directory);
    }

    @Override
    public String[] getExchangeIdsByRange(int fromIndex, int toIndex) throws AuditorException {
        throw new AuditorException("getExchangeIdsByRange currently unsupported by FileAuditor");
    }

    @Override
    public MessageExchange[] getExchangesByIds(String[] ids) throws AuditorException {
        throw new AuditorException("getExchangeByIds currently unsupported by FileAuditor");
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "File-based auditing service";
    }

    public void afterPropertiesSet() throws Exception {
        init(getContainer());
        if (autostart) {
            start();
        } else {
            stop();
        }
    }
    
    /*
     * Convenience PrintWriter implementation
     */
    private final class MessageExchangeWriter extends PrintWriter {
      
        private MessageExchangeWriter(OutputStream os) {
            super(os);
        }
        
        private void writeMessageExchange(MessageExchange exchange) {
            println("-- Exchange " + exchange.getExchangeId() + " --");
            writeProperty("endpoint", exchange.getEndpoint());
            writeProperty("MEP", exchange.getPattern());
            for (Object key : exchange.getPropertyNames()) {
                writeProperty(key, exchange.getProperty(key.toString()));
            }
        }
        
        private void writeNormalizedMessage(NormalizedMessage message) {
            for (Object key : message.getPropertyNames()) {
                writeProperty(key, message.getProperty(key.toString()));
            }
            println(); println("- content -");
        }

        
        private void writeProperty(Object key, Object value) {
            println(String.format(" %s : %s", key, value));
        }
    }
    
    /*
     * Default FileAuditorStrategy implementation, writing audit files in a folder per day
     */
    private class FileAuditorStrategyImpl implements FileAuditorStrategy {
        
        private final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        
        public String getFileName(MessageExchange exchange) {
            return dateformat.format(new Date()) + File.separatorChar + exchange.getExchangeId().replaceAll("[:\\.]", "_");
        }
    }

}
