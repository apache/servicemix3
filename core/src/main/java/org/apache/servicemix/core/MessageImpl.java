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
package org.apache.servicemix.core;

import org.apache.servicemix.api.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision: $
 * @since 4.0
 */
public class MessageImpl implements Message {

    /**
	 * Generated serial version UID 
	 */
	private static final long serialVersionUID = -8621182821298293687L;

	private Object content;
    private String contentType;
    private String contentEncoding;
    private Map<String, Object> headers;
    private Map<String, Object> attachments;

    public MessageImpl() {
    }

    public Object getContent() {
        return content;
    }

    public <T> T getContent(Class<T> type) {
        // TODO: use converters
        if (type.isInstance(content)) {
            return (T) content;
        }
        return null;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    /**
     * Get the mime content type describing the content of the message
     *
     * @return the mime content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the mime content type describing the content of the message
     *
     * @param type the mime content type
     */
    public void setContentType(String type) {
        this.contentType = type;
    }

    /**
     * Get the encoding of the message
     *
     * @return the encoding
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Set the encoding of the message
     *
     * @param encoding the encoding
     */
    public void setContentEncoding(String encoding) {
        this.contentEncoding = encoding;
    }

    public Object getHeader(String name) {
        if (headers == null) {
            return null;
        }
        return headers.get(name);
    }

    public <T> T getHeader(String name, Class<T> type) {
        if (headers == null) {
            return null;
        }
        return (T) headers.get(name);
    }

    public <T> T getHeader(Class<T> type) {
        if (headers == null) {
            return null;
        }
        return (T) headers.get(type.getName());
    }

    public void setHeader(String name, Object value) {
        if (headers == null) {
            headers = new HashMap<String, Object>();
        }
        headers.put(name, value);
    }

    public <T> void setHeader(Class<T> type, T value) {
        if (headers == null) {
            headers = new HashMap<String, Object>();
        }
        headers.put(type.getName(), value);
    }

    public Map<String, Object> getHeaders() {
        if (headers == null) {
            headers = new HashMap<String, Object>();
        }
        return headers;
    }

    public Object getAttachment(String id) {
        if (attachments != null) {
            return null;
        }
        return attachments.get(id);
    }

    public void addAttachment(String id, Object value) {
        if (attachments != null) {
            attachments = new HashMap<String, Object>();
        }
        attachments.put(id, value);
    }

    public void removeAttachment(String id) {
        if (attachments != null) {
            attachments.remove(id);
        }
    }

    public Map<String, Object> getAttachments() {
        if (attachments != null) {
            attachments = new HashMap<String, Object>();
        }
        return attachments;
    }

    public void ensureReReadable() {
        // TODO: implement        
    }

    public void copyFrom(Message msg) {
        content = msg.getContent();
        if (!msg.getHeaders().isEmpty()) {
            headers = new HashMap<String, Object>();
            for (Map.Entry<String, Object> e : msg.getHeaders().entrySet()) {
                headers.put(e.getKey(), e.getValue());
            }
        } else {
            headers = null;
        }
        if (!msg.getAttachments().isEmpty()) {
            attachments = new HashMap<String, Object>();
            for (Map.Entry<String, Object> e : msg.getAttachments().entrySet()) {
                attachments.put(e.getKey(), e.getValue());
            }
        } else {
            attachments = null;
        }
    }

    public Message copy() {
        MessageImpl copy = new MessageImpl();
        copy.copyFrom(this);
        return copy;
    }

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		ensureReReadable();
		out.defaultWriteObject();
	}

	public String display(boolean displayContent) {
		if (displayContent) {
			ensureReReadable();
		}
		return "Message []";
	}

	public String toString() {
		return display(true);
	}
}
