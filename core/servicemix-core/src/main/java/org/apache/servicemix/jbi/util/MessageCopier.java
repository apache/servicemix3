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
package org.apache.servicemix.jbi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * A thread-safe copier for NormalizedMessage onjects.
 * 
 * @author Martin Krasser
 * @deprecated use {@link org.apache.servicemix.jbi.transformer.CopyTransformer} instead
 */
public class MessageCopier {

    private boolean copySubject;
    private boolean copyContent;
    private boolean copyProperties;
    private boolean copyAttachments;

    /**
     * Creates a new message copier instance that creates full (deep) message
     * copies.
     */
    public MessageCopier() {
        this(true, true, true, true);
    }
    
    /**
     * Create a new message copier instance.
     * 
     * @param copySubject <code>true</code> if subject shall be copied.
     * @param copyContent <code>true</code> if content shall be copied
     * @param copyProperties <code>true</code> if properties shall be copied 
     * @param copyAttachments <code>true</code> if attachments shall be copied
     */
    public MessageCopier(boolean copySubject, boolean copyContent, boolean copyProperties, boolean copyAttachments) {
        super();
        this.copySubject = copySubject;
        this.copyContent = copyContent;
        this.copyProperties = copyProperties;
        this.copyAttachments = copyAttachments;
    }

    /**
     * Copies messages under consideration of the <code>copySubject</code>,
     * <code>copyContent</code>, <code>copyProperties</code>,
     * <code>copyAttachments</code> properties.
     * 
     * @param message original message.
     * @return a copy of the original message.
     * @throws MessagingException if a system-level exception occurs.
     */
    public NormalizedMessage copy(NormalizedMessage message) throws MessagingException {
        NormalizedMessage copy = new MessageUtil.NormalizedMessageImpl();
        if (copySubject) {
            copySubject(message, copy);
        }
        if (copyContent) {
            copyContent(message, copy);
        }
        if (copyProperties) {
            copyProperties(message, copy);
        }
        if (copyAttachments) {
            copyAttachments(message, copy);
        }
        return copy;
    }
    
    public boolean isCopyAttachments() {
        return copyAttachments;
    }

    public boolean isCopyContent() {
        return copyContent;
    }

    public boolean isCopyProperties() {
        return copyProperties;
    }

    public boolean isCopySubject() {
        return copySubject;
    }

    private static void copySubject(NormalizedMessage from, NormalizedMessage to) {
        to.setSecuritySubject(from.getSecuritySubject());
    }
    
    private static void copyContent(NormalizedMessage from, NormalizedMessage to) throws MessagingException {
        String str = null; 
        try {
            str = new SourceTransformer().toString(from.getContent());
        } catch (Exception e) {
            throw new MessagingException(e);
        }
        if (str != null) {
            to.setContent(new StringSource(str));
        }
    }
    
    private static void copyProperties(NormalizedMessage from, NormalizedMessage to) {
        for (Object name : from.getPropertyNames()) {
            to.setProperty((String)name, from.getProperty((String)name));
        }
    }
    
    private static void copyAttachments(NormalizedMessage from, NormalizedMessage to) throws MessagingException {
        for (Object name : from.getAttachmentNames()) {
            DataHandler handler = from.getAttachment((String)name);
            DataSource source = handler.getDataSource();
            if (!(source instanceof ByteArrayDataSource)) {
                DataSource copy = copyDataSource(source);
                handler = new DataHandler(copy);
            }
            to.addAttachment((String)name, handler);
        }
    }
    
    private static DataSource copyDataSource(DataSource source) throws MessagingException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copyInputStream(source.getInputStream(), baos);
            ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), source.getContentType());
            bads.setName(source.getName());
            return bads;
        } catch (IOException e) {
            throw new MessagingException(e);
        }
    }
    
}
