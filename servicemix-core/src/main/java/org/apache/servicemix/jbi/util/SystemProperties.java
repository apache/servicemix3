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
package org.apache.servicemix.jbi.util;

import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;

/**
 * Spring bean for initializing system properties.
 * 
 * @version $Revision: 67 $
 * @org.apache.xbean.XBean 
 */ 
public class SystemProperties implements InitializingBean {

    private Map properties;
    
    public Map getProperties() {
        return this.properties;
    }
    
    public void setProperties(Map properties) {
        this.properties = properties;
    }
    
    public void afterPropertiesSet() {
        if (this.properties != null) {
            for (Iterator it = this.properties.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                System.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

}
