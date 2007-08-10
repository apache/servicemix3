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

import java.beans.PropertyChangeListener;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

/**
 * An object to be managed can implement this class
 * to provide more meta infomation for the MBeanInfo
 *
 * @version $Revision$
 */
public interface MBeanInfoProvider {
    
    /**
     * Get an array of MBeanAttributeInfo
     * @return array of AttributeInfos
     * @throws JMException
     */
    MBeanAttributeInfo[] getAttributeInfos() throws JMException;
    
    /**
     * Get an array of MBeanOperationInfo
     * @return array of OperationInfos
     * @throws JMException
     */
    MBeanOperationInfo[] getOperationInfos() throws JMException;
    
    /**
     * Get the Object to Manage
     * @return the Object to Manage
     */
    Object getObjectToManage();
    
    /**
     * Get the name of the item
     * @return the name
     */
    String getName();
    
    /**
     * Get the type of this mbean
     * @return the type
     */
    String getType();
    
    /**
     * Get the type of this mbean
     * @return the type
     */
    String getSubType();
    
    /**
     * Get the Description of the item
     * @return the description
     */
    String getDescription();
    
    /**
     * Register for propertyChange events
     * @param l
     */
    void setPropertyChangeListener(PropertyChangeListener l);

}