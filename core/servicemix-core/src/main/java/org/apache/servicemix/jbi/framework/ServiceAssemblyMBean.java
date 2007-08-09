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
package org.apache.servicemix.jbi.framework;

import javax.jbi.management.DeploymentServiceMBean;
import javax.management.ObjectName;


public interface ServiceAssemblyMBean {

    String STARTED = DeploymentServiceMBean.STARTED;

    String SHUTDOWN = DeploymentServiceMBean.SHUTDOWN;

    String STOPPED = DeploymentServiceMBean.STOPPED;

    String getName();
    
    String getDescription();
    
    String getCurrentState();
    
    String getDescriptor();
    
    ObjectName[] getServiceUnits();

    String start() throws Exception;

    String stop() throws Exception;

    String shutDown() throws Exception;

}
