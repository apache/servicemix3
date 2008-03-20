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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.beanflow.State;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Uses reflection to find all of the fields which implement {@link State}
 * 
 * @version $Revision: $
 */
public class FieldIntrospector implements Introspector {

    private static final Log log = LogFactory.getLog(FieldIntrospector.class);

    public Iterator<State> iterator(Object owner) {
        List<State> list = new ArrayList<State>();
        for (Class type = owner.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                Object value = null;
                try {
                    field.setAccessible(true);
                    value = field.get(owner);
                }
                catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not access field: " + field + " reason: " + e, e);
                    }
                }
                State state = toState(value);
                if (state != null) {
                    list.add(state);
                }
            }
        }
        return list.iterator();
    }

    /**
     * Converts the given value to a state object or returns null if it can not
     * be converted
     */
    protected State toState(Object value) {
        if (value instanceof State) {
            return (State) value;
        }
        return null;
    }
}
