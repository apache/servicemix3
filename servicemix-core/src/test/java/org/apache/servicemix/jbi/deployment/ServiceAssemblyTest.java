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
package org.apache.servicemix.jbi.deployment;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.servicemix.schemas.deployment.Connection;
import org.apache.servicemix.schemas.deployment.Identification;
import org.apache.servicemix.schemas.deployment.ServiceAssembly;
import org.apache.servicemix.schemas.deployment.ServiceUnit;

/**
 * @version $Revision$
 */
public class ServiceAssemblyTest extends DeploymentTest {

    public void testParse() throws Exception {
        assertNotNull("JBI descriptor not found", root);

        ServiceAssembly serviceAssembly = root.getServiceAssembly();
        assertNotNull("serviceAssembly is null", serviceAssembly);

        Identification identification = serviceAssembly.getIdentification();
        assertNotNull("identification is null", identification);
        assertEquals("getName", "ServiceAssembly_041207153211-0800_saId", identification.getName());
        assertEquals("getDescription", "Description of Service Assembly : ServiceAssembly", identification.getDescription());

        List<ServiceUnit> serviceUnits = serviceAssembly.getServiceUnit();
        assertNotNull("serviceUnits are null", serviceUnits);
        assertEquals("serviceUnits size", 4, serviceUnits.size());

        assertEquals("getIdentification().getName() for 0", "TransformationSU_041207152821-0800_suId", serviceUnits.get(0).getIdentification().getName());
        assertEquals("getIdentification().getDescription() for 0", "Description of Serviceunit: TransformationSU", serviceUnits.get(0).getIdentification().getDescription());
        assertEquals("getTarget().getArtifactsZip() for 0", "TransformationSU.zip", serviceUnits.get(0).getTarget().getArtifactsZip());
        assertEquals("getTarget().getComponentName() for 0", "SunTransformationEngine", serviceUnits.get(0).getTarget().getComponentName());

        assertEquals("getIdentification().getName() for 3", "SequencingEngineSU_041207152507-0800_suId", serviceUnits.get(3).getIdentification().getName());
        assertEquals("getIdentification().getDescription() for 3", "Description of Serviceunit: SequencingEngineSU", serviceUnits.get(3).getIdentification().getDescription());
        assertEquals("getTarget().getArtifactsZip() for 3", "SequencingEngineSU.zip", serviceUnits.get(3).getTarget().getArtifactsZip());
        assertEquals("getTarget().getComponentName() for 3", "SunSequencingEngine", serviceUnits.get(3).getTarget().getComponentName());
        
        List<Connection> connections = serviceAssembly.getConnections().getConnection();
        assertNotNull("connections are null", connections);
        assertEquals("connections size", 2, connections.size());
        
        assertEquals("getConsumer().getServiceName() for 0", new QName("http://www.gaiati.com/emee/ns/csi", "csi-service"), connections.get(0).getConsumer().getServiceName());
    }

    protected URL getDescriptorURL() throws Exception {
        return getClass().getResource("serviceAssembly.xml");
    }

}
