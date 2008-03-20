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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;

import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * A simple holder for an Attribute and a PropertyDescriptor
 * 
 * @version $Revision$
 */
public class CachedAttribute {
    private Object bean;
    private String name;
    private Attribute attribute;
    private MBeanAttributeInfo attributeInfo;
    private PropertyDescriptor propertyDescriptor;

    /**
     * Constructor
     * 
     * @param attr
     */
    public CachedAttribute(Attribute attr) {
        this.attribute = attr;
        this.name = attr.getName();
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the attribute.
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Set the Attribute
     * 
     * @param attribute
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * Ensdure attribute value is up to date
     * 
     * @param beanUtil
     * @throws MBeanException
     */
    public void updateValue(PropertyUtilsBean beanUtil) throws MBeanException {
        try {
            Object value = beanUtil.getProperty(bean, getName());
            if (value != attribute.getValue()) {
                this.attribute = new Attribute(getName(), value);
            }
        } catch (IllegalAccessException e) {
            throw new MBeanException(e);
        } catch (InvocationTargetException e) {
            throw new MBeanException(e);
        } catch (NoSuchMethodException e) {
            throw new MBeanException(e);
        }
    }

    /**
     * Update the Attribute
     * 
     * @param beanUtils
     * @param attr The attribute to set.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public void updateAttribute(PropertyUtilsBean beanUtils, Attribute attr) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (this.attribute != null && propertyDescriptor != null) {
            // update object value
            beanUtils.setProperty(bean, getName(), attr.getValue());
        }
        this.attribute = attr;
    }

    /**
     * Update that attribute value
     * 
     * @param value
     */
    public void updateAttributeValue(Object value) {
        this.attribute = new Attribute(this.attribute.getName(), value);
    }

    /**
     * @return Returns the bean.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * @param bean The bean to set.
     */
    public void setBean(Object bean) {
        this.bean = bean;
    }

    /**
     * @return Returns the propertyDescriptor.
     */
    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    /**
     * @param propertyDescriptor The propertyDescriptor to set.
     */
    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    /**
     * @return Returns the attributeInfo.
     */
    public MBeanAttributeInfo getAttributeInfo() {
        return attributeInfo;
    }

    /**
     * @param attributeInfo The attributeInfo to set.
     */
    public void setAttributeInfo(MBeanAttributeInfo attributeInfo) {
        this.attributeInfo = attributeInfo;
    }
}