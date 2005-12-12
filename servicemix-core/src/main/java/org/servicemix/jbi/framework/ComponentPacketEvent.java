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
package org.servicemix.jbi.framework;

import java.io.Serializable;

/**
 * ComponentPacket - potentially passed around clusters
 *
 * @version $Revision$
 */
public class ComponentPacketEvent implements Serializable {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 6365244552146210991L;
    /**
     * ComponentConnector activated
     */
    public static final int ACTIVATED = 1;
    /**
     * ComponentConnector deactivated
     */
    public static final int DEACTIVATED = 2;
    
    /**
     * ComponentConnector changed it's state
     */
    public static final int STATE_CHANGE  = 3;
    
    
    private int status = ACTIVATED;
    private ComponentPacket packet;
    
    /**
     * Default Constructor
     *
     */
    public ComponentPacketEvent(){
    }
    
    /**
     * Construct an event
     * @param packet
     * @param status
     */
    public ComponentPacketEvent(ComponentPacket packet, int status) {
        this.packet = packet;
        this.status = status;
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
     * @return Returns the status.
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * @return pretty print
     */
    public String toString(){
        return "ComponentPacketEvent[status=" + ComponentPacketEvent.getStatusAsString(status) + ",packet=" + packet + "]";
    }
    
    /**
     * Get the status as a String
     * @param status
     * @return String representation of the status
     */
    public static String getStatusAsString(int status)  {
        String result = null;
        switch (status) {
        	case ACTIVATED: 
        		result = "activated";
        		break;
        	case DEACTIVATED:
        		result = "deactivated";
        		break;
        	case STATE_CHANGE:
        		result = "changed";
        		break;
        	default:
        		result = "unknown";
        }
        return result;
    }
}
