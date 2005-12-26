/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package org.apache.servicemix.jbi.framework;
import javax.jbi.servicedesc.ServiceEndpoint;

import java.util.Set;

/**
 * ComponentConnector is used internally for message routing
 * 
 * @version $Revision$
 */
public class ComponentConnector {
    protected ComponentPacket packet;
    

    /**
     * Default Constructor
     */
    public ComponentConnector() {
        this.packet = new ComponentPacket();
    }

    /**
     * Construct with it's name
     * 
     * @param name
     */
    public ComponentConnector(ComponentNameSpace name) {
        this.packet = new ComponentPacket(name);
    }

    /**
     * Construct a ComponentConnector from a ComponentPacket
     * 
     * @param packet
     */
    public ComponentConnector(ComponentPacket packet) {
        this.packet = packet;
    }
    
    /**
     * @return true if the Component is local to the Container
     */
    public boolean isLocal(){
        return false;
    }

    
    /**
     * @return Returns the packet.
     */
    public ComponentPacket getPacket() {
        return packet;
    }

    /**
     * @param packet The packet to set.
     */
    public void setPacket(ComponentPacket packet) {
        this.packet = packet;
    }

    /**
     * @return the ComponentPacket that holds state infomation for this Connector
     */
    public ComponentPacket getComponentPacket() {
        return packet;
    }

    /**
     * @return Returns the ComponentName.
     */
    public ComponentNameSpace getComponentNameSpace() {
        return packet.getComponentNameSpace();
    }

   
    /**
     * Get the Set of activated endpoints
     * 
     * @return the activated endpoint Set
     */
    public Set getActiveEndpoints() {
        return packet.getActiveEndpoints();
    }

    /**
     * Add an activated endpoint
     * 
     * @param endpoint
     */
    public void addActiveEndpoint(ServiceEndpoint endpoint) {
        throw new RuntimeException("Not a LocalComponentConnector");
    }

    /**
     * remove an activated endpoint
     * 
     * @param endpoint
     */
    public void removeActiveEndpoint(ServiceEndpoint endpoint) {
        throw new RuntimeException("Not a LocalComponentConnector");
    }

    /**
     * Add an external activated endpoint
     * 
     * @param endpoint
     */
    public void addExternalActiveEndpoint(ServiceEndpoint endpoint) {
        throw new RuntimeException("Not a LocalComponentConnector");
    }

    /**
     * remove an external activated endpoint
     * 
     * @param endpoint
     */
    public void removeExternalActiveEndpoint(ServiceEndpoint endpoint) {
        throw new RuntimeException("Not a LocalComponentConnector");
    }

    /**
     * Get the Set of external activated endpoints
     * 
     * @return the activated endpoint Set
     */
    public Set getExternalActiveEndpoints() {
        return packet.getExternalActiveEndpoints();
    }
    
    /**
     * @return Returns the binding.
     */
    public boolean isBinding() {
        return packet.isBinding();
    }
    /**
     * @return Returns the service.
     */
    public boolean isService() {
        return packet.isService();
    }
    
    public String toString() {
        return "ComponentConnector[" + packet.getComponentNameSpace() + "]";
    }
}
