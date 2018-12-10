/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.BaseLifeCycle;

import org.apache.ode.bpe.bped.EventDirector;
import org.apache.ode.bpe.bped.EventDirectorFactory;
import org.apache.ode.bpe.util.BPEProperties;

public class BPELifeCycle extends BaseLifeCycle {

    public static final String IM_ENGINE_PROPERTY_FILE_NAME = "bpeEngine.properties";
    
    private EventDirector eventDirector;
    
	public BPELifeCycle(BaseComponent component) {
		super(component);
	}

    public EventDirector getEventDirector() {
        return eventDirector;
    }

    protected void doInit() throws Exception {
        BPEProperties props = BPEProperties.getCachedProperties();
        props.load(getClass().getClassLoader().getResourceAsStream(IM_ENGINE_PROPERTY_FILE_NAME));
        eventDirector = EventDirectorFactory.createEventDirector(props);
        super.doInit();
    }

}
