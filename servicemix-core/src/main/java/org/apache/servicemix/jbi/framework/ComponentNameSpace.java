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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Component Name is used internally to identify a Component.
 * 
 * @version $Revision$
 */
public class ComponentNameSpace implements Externalizable {
   
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -9130913368962887486L;
    
    protected String containerName;
    protected String name;
    protected String componentId;

    /**
     * Default Constructor
     */
    public ComponentNameSpace() {
    }

    /**
     * Construct a ComponentName
     * 
     * @param containerName
     * @param componentName
     * @param componentId
     */
    public ComponentNameSpace(String containerName, String componentName, String componentId) {
        this.containerName = containerName;
        this.name = componentName;
        this.componentId = componentId;
        if (this.name == null) {
            this.name = this.componentId;
        }
        if (this.componentId == null) {
            this.componentId = this.name;
        }
    }
    
    /**
     * @return Returns the componentId.
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * @param componentId The componentId to set.
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * @return Returns the componentName.
     */
    public String getName() {
        return name;
    }

    /**
     * @param componentName The componentName to set.
     */
    public void setName(String componentName) {
        this.name = componentName;
    }

    /**
     * @return Returns the containerName.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * @param containerName The containerName to set.
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    
    /**
     * @param obj
     * @return true if obj is equivalent to 'this'
     */
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && obj instanceof ComponentNameSpace) {
            ComponentNameSpace other = (ComponentNameSpace) obj;
            result = other.getContainerName().equals(this.containerName)
                    && other.getComponentId().equals(this.componentId);
        }
        return result;
    }
    
    /**
     * @return the hashCode
     */
    public int hashCode() {
        return containerName.hashCode() ^ componentId.hashCode();
    }
    
    /**
     * @return pretty print
     */
    public String toString() {
        return "[container=" + containerName + ",name=" + name + ",id=" + componentId + "]";
    }

    /**
     * write out to stream
     * @param out
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(containerName != null ? containerName : "");
        out.writeUTF(name != null ? name : "");
        out.writeUTF(componentId != null ? componentId : "");
    }

    /**
     * read from Stream
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        containerName = in.readUTF();
        name = in.readUTF();
        componentId = in.readUTF();
    }
    
    /**
     * copy this
     * @return
     */
    public ComponentNameSpace copy(){
        ComponentNameSpace result = new ComponentNameSpace(containerName,name,componentId);
        return result;
    }
    
}
