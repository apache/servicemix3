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
package org.apache.servicemix.jbi.management;

import java.util.concurrent.ExecutorService;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.StandardMBean;

/**
 * Builds a DynamicMBean wrappers for existing objects
 * 
 * @version $Revision$
 */
public final class MBeanBuilder {
    
    private MBeanBuilder() {
    }

    /**
     * Build an MBean
     * 
     * @param theObject
     * @param interfaceMBean
     * @param description
     * @param executorService 
     * @return the MBean wrapper
     * @throws JMException
     */
    static DynamicMBean buildStandardMBean(Object theObject, Class interfaceMBean, 
                                           String description, ExecutorService executorService) throws JMException {
        DynamicMBean result = null;
        if (theObject != null) {
            if (theObject instanceof MBeanInfoProvider) {
                MBeanInfoProvider info = (MBeanInfoProvider) theObject;
                result = new BaseStandardMBean(
                        info.getObjectToManage(),
                        interfaceMBean, 
                        description, 
                        info.getAttributeInfos(), 
                        info.getOperationInfos(),
                        executorService);
                info.setPropertyChangeListener((BaseStandardMBean)result);
            } else {
                return new StandardMBean(theObject, interfaceMBean);
            }
        }
        return result;
    }
}
