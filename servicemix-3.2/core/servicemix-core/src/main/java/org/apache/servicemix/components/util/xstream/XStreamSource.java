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
package org.apache.servicemix.components.util.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.TraxSource;

import org.apache.servicemix.JavaSource;

/**
 * A {@link javax.xml.transform.Source} implementation for XStream which implements the
 * {@link JavaSource} API
 *
 * @version $Revision$
 */
public class XStreamSource extends TraxSource implements JavaSource {
    private Object object;

    public XStreamSource() {
    }

    public XStreamSource(Object object) {
        super(object);
        this.object = object;
    }

    public XStreamSource(Object object, XStream xStream) {
        super(object, xStream);
        this.object = object;
    }

    public Object getObject() {
        return this.object;
    }

    public void setObject(Object object) {
        setSource(object);
    }

    public void setSource(Object source) {
        super.setSource(source);
        this.object = source;
    }
}
