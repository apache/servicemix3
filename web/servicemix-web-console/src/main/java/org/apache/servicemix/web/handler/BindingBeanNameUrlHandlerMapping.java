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
package org.apache.servicemix.web.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

/**
 * 
 * @version $Revision: 426366 $
 */
public class BindingBeanNameUrlHandlerMapping extends BeanNameUrlHandlerMapping {

    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        Object object = super.getHandlerInternal(request);

        if (object instanceof String) {
            String handlerName = (String) object;
            object = getApplicationContext().getBean(handlerName);
        }

        // support both Spring 2.0 that returns a Handler
        // and Spring 2.5 that returns a HandlerExecutionChain
        Object handler = object;
        if (object instanceof HandlerExecutionChain) {
            handler = ((HandlerExecutionChain) object).getHandler();
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(handler, null);
        binder.bind(request);
        return object;
    }

}
