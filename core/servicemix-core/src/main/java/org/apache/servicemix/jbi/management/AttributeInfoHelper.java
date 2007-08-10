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
import java.util.ArrayList;
import java.util.List;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;

import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * A Helper class to build a list of MBeanAttributInfos
 * 
 * @version $Revision$
 */
public class AttributeInfoHelper {
    private PropertyUtilsBean beanUtil = new PropertyUtilsBean();

    private List<MBeanAttributeInfo> list = new ArrayList<MBeanAttributeInfo>();

    /**
     * Add an attribute
     * 
     * @param theObject
     * @param name
     * @param description
     * @throws ReflectionException
     * 
     */
    public void addAttribute(Object theObject, String name, String description) throws ReflectionException {
        PropertyDescriptor pd;
        try {
            pd = beanUtil.getPropertyDescriptor(theObject, name);
            MBeanAttributeInfo info = new MBeanAttributeInfo(name, description, pd.getReadMethod(), pd.getWriteMethod());
            list.add(info);
        } catch (IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        }

    }

    /**
     * Get array of MBeanAttriubteInfos registered
     * 
     * @return MBeanAttributeInfos
     */
    public MBeanAttributeInfo[] getAttributeInfos() {
        MBeanAttributeInfo[] result = new MBeanAttributeInfo[list.size()];
        list.toArray(result);
        return result;
    }

    /**
     * clear the internal list
     */
    public void clear() {
        list.clear();
    }

    /**
     * Join two MBeanAttributeInfo[] arrays
     * 
     * @param attrs1
     * @param attrs2
     * @return new MBeanAttributeInfo array containing contents of attr1 and
     *         attr2
     */
    public static MBeanAttributeInfo[] join(MBeanAttributeInfo[] attrs1, MBeanAttributeInfo[] attrs2) {
        MBeanAttributeInfo[] result = null;
        int length = 0;
        int startPos = 0;
        if (attrs1 != null) {
            length = attrs1.length;
        }
        if (attrs2 != null) {
            length += attrs2.length;
        }

        result = new MBeanAttributeInfo[length];
        if (attrs1 != null) {
            System.arraycopy(attrs1, 0, result, startPos, attrs1.length);
            startPos = attrs1.length;
        }
        if (attrs2 != null) {
            System.arraycopy(attrs2, 0, result, startPos, attrs2.length);
        }
        return result;
    }
}