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
package org.apache.servicemix.bpe;

import java.io.File;

import org.apache.servicemix.common.BaseLifeCycle;
import org.apache.servicemix.common.ServiceUnit;
import org.springframework.core.io.Resource;

/**
 * 
 * @author gnodet
 * @version $Revision: 376451 $
 * @org.apache.xbean.XBean element="component"
 *                  description="A BPE component"
 */
public class BPESpringComponent extends BPEComponent {

    private String name;
    private Resource bpelResource;
    
    /* (non-Javadoc)
     * @see org.servicemix.common.BaseComponent#createLifeCycle()
     */
    protected BaseLifeCycle createLifeCycle() {
        return new LifeCycle();
    }

    public class LifeCycle extends BPELifeCycle {

        protected ServiceUnit su;
        
        public LifeCycle() {
            super(BPESpringComponent.this);
        }
        
        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doInit()
         */
        protected void doInit() throws Exception {
            super.doInit();
            if (bpelResource == null) {
                throw new IllegalArgumentException("bpelResource must be configured");
            }
            File bpelFile = bpelResource.getFile();
            String fileName = bpelFile.getName();
            if (!fileName.endsWith(".bpel")) {
                throw new IllegalArgumentException("bpelResource must resolve to a .bpel file");
            }
            if (name == null) {
                name = fileName.substring(0, fileName.length() - 5);
            }
            su = new BPEDeployer(BPESpringComponent.this).deploy(name, bpelFile.getParent());
            getRegistry().registerServiceUnit(su);
        }

        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doStart()
         */
        protected void doStart() throws Exception {
            super.doStart();
            su.start();
        }
        
        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doStop()
         */
        protected void doStop() throws Exception {
            su.stop();
            super.doStop();
        }
        
        /* (non-Javadoc)
         * @see org.servicemix.common.BaseLifeCycle#doShutDown()
         */
        protected void doShutDown() throws Exception {
            su.shutDown();
            super.doShutDown();
        }
    }

    /**
     * @return Returns the bpelResource.
     */
    public Resource getBpelResource() {
        return bpelResource;
    }

    /**
     * @param bpelResource The bpelResource to set.
     */
    public void setBpelResource(Resource bpelResource) {
        this.bpelResource = bpelResource;
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

}
