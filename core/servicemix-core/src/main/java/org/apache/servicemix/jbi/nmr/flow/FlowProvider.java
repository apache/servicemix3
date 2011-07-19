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
package org.apache.servicemix.jbi.nmr.flow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.jbi.JBIException;

import org.apache.servicemix.finder.FactoryFinder;
import org.apache.servicemix.jbi.util.IntrospectionSupport;
import org.apache.servicemix.jbi.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find a Flow by Name
 * 
 * @version $Revision$
 */
public final class FlowProvider {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(FlowProvider.class);

    private static final FactoryFinder FINDER = new FactoryFinder("META-INF/services/org/apache/servicemix/jbi/nmr/flow/");

    private FlowProvider() {
    }

    /**
     * Locate a Flow
     * 
     * @param flow
     * @return the Flow
     * @throws JBIException
     */
    public static Flow getFlow(String flow) throws JBIException {
        Object value;
        String flowName = getFlowName(flow);
        try {
            value = FINDER.newInstance(flowName);
            if (value instanceof Flow) {
                String query = getQuery(flow);
                if (query != null) {
                    Map map = URISupport.parseQuery(query);
                    if (map != null && !map.isEmpty()) {
                        IntrospectionSupport.setProperties(value, map);
                    }
                }
                return (Flow) value;
            }
            throw new JBIException("No implementation found for: " + flow);
        } catch (IllegalAccessException e) {
            LOGGER.error("getFlow(" + flow + " failed: " + e, e);
            throw new JBIException(e);
        } catch (InstantiationException e) {
            LOGGER.error("getFlow(" + flow + " failed: " + e, e);
            throw new JBIException(e);
        } catch (IOException e) {
            LOGGER.error("getFlow(" + flow + " failed: " + e, e);
            throw new JBIException(e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("getFlow(" + flow + " failed: " + e, e);
            throw new JBIException(e);
        } catch (URISyntaxException e) {
            LOGGER.error("getFlow(" + flow + " failed: " + e, e);
            throw new JBIException(e);
        }
    }

    public static String getFlowName(String str) {
        String result = str;
        int index = str.indexOf('?');
        if (index >= 0) {
            result = str.substring(0, index);
        }
        return result;
    }

    protected static String getQuery(String str) {
        String result = null;
        int index = str.indexOf('?');
        if (index >= 0 && (index + 1) < str.length()) {
            result = str.substring(index + 1);
        }
        return result;
    }

}