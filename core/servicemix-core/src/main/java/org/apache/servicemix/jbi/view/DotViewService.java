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
package org.apache.servicemix.jbi.view;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.management.JMException;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="dotViewService"
 */
public class DotViewService extends BaseSystemService implements InitializingBean, DotViewServiceMBean {

    private JBIContainer container;
    private boolean autoStart = true;
    private DotViewEndpointListener endpointListener;
    private DotViewFlowListener flowListener;

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public JBIContainer getContainer() {
        return container;
    }

    public void setContainer(JBIContainer container) {
        this.container = container;
    }
    
    protected Class getServiceMBean() {
        return DotViewServiceMBean.class;
    }

    public String getDescription() {
        return "DotView service";
    }

    /* (non-Javadoc)
     * @see javax.jbi.management.LifeCycleMBean#start()
     */
    public void start() throws javax.jbi.JBIException {
        super.start();
        this.container.addListener(endpointListener);
        this.container.addListener(flowListener);
    }

    /* (non-Javadoc)
     * @see javax.jbi.management.LifeCycleMBean#stop()
     */
    public void stop() throws javax.jbi.JBIException {
        this.container.removeListener(endpointListener);
        this.container.removeListener(flowListener);
        super.stop();
    }

    public void afterPropertiesSet() throws Exception {
        if (this.container == null) {
            throw new IllegalArgumentException("container should not be null");
        }
        init(getContainer());
        endpointListener = new DotViewEndpointListener();
        endpointListener.setContainer(container);
        endpointListener.setRerenderOnChange(false);
        flowListener = new DotViewFlowListener();
        flowListener.setContainer(container);
        flowListener.setRerenderOnChange(false);
        if (autoStart) {
            start();
        } else {
            stop();
        }
    }
    
    public String createEndpointGraph() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        endpointListener.generateFile(pw);
        String str = sw.toString();
        return str;
    }
    
    public String createFlowGraph()  throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        flowListener.generateFile(pw);
        String str = sw.toString();
        return str;
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "createEndpointGraph", 0, "create an endpoint dot graph");
        helper.addOperation(getObjectToManage(), "createFlowGraph", 0, "create an flow dot graph");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

}
