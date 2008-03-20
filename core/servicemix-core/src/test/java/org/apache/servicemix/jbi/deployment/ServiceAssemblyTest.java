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
package org.apache.servicemix.jbi.deployment;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class ServiceAssemblyTest extends TestCase {

    public void testParse() throws Exception {

        // lets force the JBI container to be constructed first
        Descriptor root = DescriptorFactory.buildDescriptor(getClass().getResource("serviceAssembly.xml"));
        assertNotNull("Unable to parse descriptor", root);

        ServiceAssembly serviceAssembly = root.getServiceAssembly();
        assertNotNull("serviceAssembly is null", serviceAssembly);

        Identification identification = serviceAssembly.getIdentification();
        assertNotNull("identification is null", identification);
        assertEquals("getName", "ServiceAssembly_041207153211-0800_saId", identification.getName());
        assertEquals("getDescription", "Description of Service Assembly : ServiceAssembly", identification.getDescription());

        ServiceUnit[] serviceUnits = serviceAssembly.getServiceUnits();
        assertNotNull("serviceUnits are null", serviceUnits);
        assertEquals("serviceUnits size", 4, serviceUnits.length);

        assertEquals("getIdentification().getName() for 0", "TransformationSU_041207152821-0800_suId", 
                     serviceUnits[0].getIdentification().getName());
        assertEquals("getIdentification().getDescription() for 0", "Description of Serviceunit: TransformationSU", 
                     serviceUnits[0].getIdentification().getDescription());
        assertEquals("getTarget().getArtifactsZip() for 0", "TransformationSU.zip", serviceUnits[0].getTarget().getArtifactsZip());
        assertEquals("getTarget().getComponentName() for 0", "SunTransformationEngine", serviceUnits[0].getTarget().getComponentName());

        assertEquals("getIdentification().getName() for 3", "SequencingEngineSU_041207152507-0800_suId", serviceUnits[3]
                        .getIdentification().getName());
        assertEquals("getIdentification().getDescription() for 3", "Description of Serviceunit: SequencingEngineSU", 
                     serviceUnits[3].getIdentification().getDescription());
        assertEquals("getTarget().getArtifactsZip() for 3", "SequencingEngineSU.zip", serviceUnits[3].getTarget().getArtifactsZip());
        assertEquals("getTarget().getComponentName() for 3", "SunSequencingEngine", serviceUnits[3].getTarget().getComponentName());

        Connection[] connections = serviceAssembly.getConnections().getConnections();
        assertNotNull("connections are null", connections);
        assertEquals("connections size", 2, connections.length);

        assertEquals("getConsumer().getServiceName() for 0", new QName("urn:csi", "csi-service"), connections[0].getConsumer()
                        .getServiceName());
    }

}
