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
package org.apache.servicemix.client;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * An implementation of {@link NamespaceContext} which uses a simple Map where
 * the keys are the prefixes and the values are the URIs
 * 
 * @version $Revision: $
 * @deprecated
 */
public class DefaultNamespaceContext extends org.apache.servicemix.jbi.jaxp.DefaultNamespaceContext {

    public DefaultNamespaceContext() {
        super();
    }

    public DefaultNamespaceContext(NamespaceContext parent, Map map) {
        super(parent, map);
    }
    
}
