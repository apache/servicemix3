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
package org.apache.servicemix.geronimo;

import java.io.Serializable;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Object which represents a reference between a ServceAssembly and
 * a ServiceUnit
 */
public class ServiceUnitReference implements Serializable {

    /**
     * The name of the ServiceUnit where the SU
     * is deployed
     */
    private Artifact associaltedServiceUnitName;

    /**
     * Name of the serviceUnit
     */
    private String serviceUnitName;

    /**
     * installPath of the serviceUnit
     */
    private String serviceUnitPath;

    public ServiceUnitReference(Artifact component, String name, String path) {
        this.associaltedServiceUnitName = component;
        this.serviceUnitName = name;
        this.serviceUnitPath = path;
    }

    public Artifact getAssocialtedServiceUnitName() {
        return associaltedServiceUnitName;
    }

    public String getServiceUnitName() {
        return serviceUnitName;
    }

    public String getServiceUnitPath() {
        return serviceUnitPath;
    }

}
