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
package org.apache.servicemix.jbi;

import javax.xml.namespace.QName;

/**
 * An exception thrown if a component cannot find an instance of a {@link ServiceEndpoint} for a given serviceName.
 *
 * @version $Revision$
 */
public class NoServiceEndpointAvailableException extends NoEndpointAvailableException {

    private final QName serviceName;
    private final String endpointName;

    public NoServiceEndpointAvailableException(QName serviceName, String endpointName) {
        super("Cannot find an instance of the service: " + serviceName + " and endpoint: " + endpointName);
        this.serviceName = serviceName;
        this.endpointName = endpointName;
    }

    /**
     * Returns the service name that could not be found
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * Returns the endpoint name that could not be found
     */
    public String getEndpointName() {
        return endpointName;
    }
}
