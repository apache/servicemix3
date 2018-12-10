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
package org.apache.servicemix.jbi.event;

import java.util.EventObject;

import org.apache.servicemix.jbi.framework.ServiceUnitLifeCycle;

public class ServiceUnitEvent extends EventObject {

    private static final long serialVersionUID = 7825652001472392923L;
    
    public static final int UNIT_DEPLOYED = 0;
    public static final int UNIT_STARTED = 1;
    public static final int UNIT_STOPPED = 2;
    public static final int UNIT_SHUTDOWN = 3;
    public static final int UNIT_UNDEPLOYED = 4;
    
    private ServiceUnitLifeCycle unit;
    private int type;
    
    public ServiceUnitEvent(ServiceUnitLifeCycle unit, int type) {
        super(unit);
        this.unit = unit;
        this.type = type;
    }
    
    public ServiceUnitLifeCycle getServiceUnit() {
        return unit;
    }

    public int getEventType() {
        return type;
    }

}
