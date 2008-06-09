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
package org.apache.servicemix.common;

import java.lang.reflect.Method;

import javax.jbi.component.ComponentContext;

public abstract class Container {

    public enum Type {
        ServiceMix3,
        ServiceMix4,
        Unknown
    }

    protected final ComponentContext context;

    protected Container(ComponentContext context) {
        this.context = context;
    }

    public String toString() {
        return getType().toString();
    }

    public abstract Type getType();

    public abstract boolean handleTransactions();

    public static Container detect(ComponentContext context) {
        try {
            String clName = context.getClass().getName();
            if ("org.apache.servicemix.jbi.framework.ComponentContextImpl".equals(clName)) {
                return new Smx3Container(context);
            }
            if ("org.apache.servicemix.jbi.runtime.impl.ComponentContextImpl".equals(clName)) {
                return new Smx4Container(context);
            }
        } catch (Throwable t) {
        }
        return new UnknownContainer(context);
    }

    public static class Smx3Container extends Container {
        public Smx3Container(ComponentContext context) {
            super(context);
        }
        public Type getType() {
            return Type.ServiceMix3;
        }
        public boolean handleTransactions() {
            try {
                Object container = getSmx3Container();
                Method isUseNewTransactionModelMth = container.getClass().getMethod("isUseNewTransactionModel", new Class[0]);
                Boolean b = (Boolean) isUseNewTransactionModelMth.invoke(container, new Object[0]);
                return !b;
            } catch (Throwable t) {
            }
            return true;
        }
        public Object getSmx3Container() {
            try {
                Method getContainerMth = context.getClass().getMethod("getContainer", new Class[0]);
                Object container = getContainerMth.invoke(context, new Object[0]);
                return container;
            } catch (Throwable t) {
            }
            return null;
        }
    }

    public static class Smx4Container extends Container {
        public Smx4Container(ComponentContext context) {
            super(context);
        }
        public Type getType() {
            return Type.ServiceMix4;
        }
        public boolean handleTransactions() {
            return false;
        }
    }

    public static class UnknownContainer extends Container {
        public UnknownContainer(ComponentContext context) {
            super(context);
        }
        public Type getType() {
            return Type.Unknown;
        }
        public boolean handleTransactions() {
            return false;
        }
    }

}
