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
package org.apache.servicemix.beanflow.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper class for working with enums
 * 
 * @version $Revision$
 */
public class EnumHelper {
    private static final Class[] NO_PARAMETER_TYPES = {};
    private static final Object[] NO_PARAMETER_VALUES = {};

    public static Object[] getEnumValues(Class enumType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = enumType.getMethod("values", NO_PARAMETER_TYPES);
        return (Object[]) method.invoke(null, NO_PARAMETER_VALUES);
    }
}
