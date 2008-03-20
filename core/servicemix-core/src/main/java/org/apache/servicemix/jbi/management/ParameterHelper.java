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
package org.apache.servicemix.jbi.management;

import javax.management.MBeanParameterInfo;

/**
 * A Helper class to build a list of MBeanParamerterInfo
 * 
 * @version $Revision$
 */
public class ParameterHelper {
    private MBeanParameterInfo[] infos;

    ParameterHelper(MBeanParameterInfo[] infos) {
        this.infos = infos;
    }

    /**
     * Decorate a paramter
     * 
     * @param index
     * @param name
     * @param description
     */
    public void setDescription(int index, String name, String description) {
        MBeanParameterInfo old = infos[index];
        infos[index] = new MBeanParameterInfo(name, old.getType(), description);
    }
}