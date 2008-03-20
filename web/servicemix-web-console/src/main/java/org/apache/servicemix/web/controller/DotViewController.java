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
package org.apache.servicemix.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.jbi.view.DotViewServiceMBean;
import org.apache.servicemix.web.view.DotView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DotViewController implements Controller {

    public static final String TYPE_FLOW = "flow";
    public static final String TYPE_ENDPOINTS = "endpoints";
    
    private DotViewServiceMBean mbean;
    private String type;
    private DotView view;
    
    public DotViewController(DotViewServiceMBean mbean) {
        this.mbean = mbean;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String dotSrc;
        if (TYPE_FLOW.equals(getType())) {
            dotSrc = mbean.createFlowGraph();
        } else if (TYPE_ENDPOINTS.equals(getType())) {
            dotSrc = mbean.createEndpointGraph();
        } else {
            throw new IllegalStateException("Unknown type: " + getType());
        }
        Map<String,Object> model = new HashMap<String,Object>();
        model.put(view.getDotModelSource(), dotSrc);
        return new ModelAndView(getView(), model);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DotView getView() {
        return view;
    }

    public void setView(DotView view) {
        this.view = view;
    }
}
