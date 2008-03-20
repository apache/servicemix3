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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * A Helper class to build an MBeanOperationInfo
 * 
 * @version $Revision$
 */
public class OperationInfoHelper {
    private List<MBeanOperationInfo> list = new ArrayList<MBeanOperationInfo>();

    /**
     * Add an operation
     * 
     * @param theObject
     * @param name
     * @param description
     * @return array of MBeanParameterInfos
     */
    public ParameterHelper addOperation(Object theObject, String name, String description) {
        return addOperation(theObject, name, 0, description);
    }

    /**
     * Add an operation
     * 
     * @param theObject
     * @param name
     * @param numberParams
     * @param description
     * @return array of MBeanParameterInfos
     */
    public ParameterHelper addOperation(Object theObject, String name, int numberParams, String description) {
        Method method = getMethod(theObject.getClass(), name, numberParams);
        MBeanOperationInfo opInfo = new MBeanOperationInfo(description, method);
        list.add(opInfo);
        MBeanParameterInfo[] result = opInfo.getSignature();
        return new ParameterHelper(result);
    }

    /**
     * Get array of MBeanOperationInfos registered
     * 
     * @return MBeanOperationInfos
     */
    public MBeanOperationInfo[] getOperationInfos() {
        MBeanOperationInfo[] result = new MBeanOperationInfo[list.size()];
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
     * Join two MBeanOperationInfo[] arrays
     * 
     * @param ops1
     * @param ops2
     * @return new MBeanOperationInfo array containing contents of ops1 and ops2
     */
    public static MBeanOperationInfo[] join(MBeanOperationInfo[] ops1, MBeanOperationInfo[] ops2) {
        MBeanOperationInfo[] result = null;
        int length = 0;
        int startPos = 0;
        if (ops1 != null) {
            length = ops1.length;
        }
        if (ops2 != null) {
            length += ops2.length;
        }
        result = new MBeanOperationInfo[length];
        if (ops1 != null) {
            System.arraycopy(ops1, 0, result, startPos, ops1.length);
            startPos = ops1.length;
        }
        if (ops2 != null) {
            System.arraycopy(ops2, 0, result, startPos, ops2.length);
        }
        return result;
    }

    private Method getMethod(Class<? extends Object> theClass, String name, int numParams) {
        Method result = null;
        Method[] methods = theClass.getMethods();
        if (methods != null) {
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(name) && methods[i].getParameterTypes().length == numParams) {
                    result = methods[i];
                    break;
                }
            }
            if (result == null) {
                // do a less exact search
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(name)) {
                        result = methods[i];
                        break;
                    }
                }
            }
        }
        return result;
    }
}