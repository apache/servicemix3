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
package org.apache.servicemix.api;

import java.io.Serializable;
import java.util.Set;

/**
 * @version $Revision: $
 * @since 4.0
 */
public interface Message extends Serializable, Cloneable {

    enum Type {
        In, Out, Fault
    }

    Object      getContent();
    void        setContent(Object content);

    Set<String> getPropertyNames();
    Object      getProperty(String name);
    void        setProperty(String name, Object value);

    Set<String> getAttachmentIds();
    Object      getAttachment(String id);
    void        addAttachment(String id, Object value);
    void        removeAttachment(String id);

    /**
     *
     * @param msg the message to copy from
     */
    void        copyFrom(Message msg);

    /**
     * Creates
     * @return
     */
    Message     copy();

    /**
     * Make sure that all streams contained in the content and in
     * attachments are transformed to re-readable sources.
     * This method will be called by the framework when persisting
     * the message or when displaying it
     */
    void        ensureReReadable();

    String      display(boolean displayContent);
}
