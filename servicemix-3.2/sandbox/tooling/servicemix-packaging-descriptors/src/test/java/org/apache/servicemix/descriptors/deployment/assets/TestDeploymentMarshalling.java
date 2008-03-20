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
package org.apache.servicemix.descriptors.deployment.assets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.servicemix.descriptors.jbi.Jbi;
import org.apache.servicemix.descriptors.packaging.assets.Assets;
import org.apache.servicemix.descriptors.packaging.assets.Components;
import org.apache.servicemix.descriptors.packaging.assets.Components.Component;
import org.w3c.dom.Element;

public class TestDeploymentMarshalling extends TestCase {

	public void testComponentXml() {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Components.class.getPackage()
					.getName());
			Unmarshaller m = context.createUnmarshaller();
			Components components = (Components) m.unmarshal(getClass()
					.getClassLoader().getResourceAsStream("components.xml"));			
			assertNotNull(components.getComponent());
			assertEquals(1, components.getComponent().size());
			Component component = components.getComponent().get(0);
			assertEquals("servicemix-lwcontainer", component.getName());
			assertNotNull(component.getAssets().getEngine());
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testJbiXml() {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Jbi.class.getPackage().getName());
			Unmarshaller m = context.createUnmarshaller();
			Jbi jbi = (Jbi) m.unmarshal(getClass().getClassLoader()
					.getResourceAsStream("jbi.xml"));
			assertNotNull(jbi.getComponent());
			assertEquals("servicemix-lwcontainer", jbi.getComponent()
					.getIdentification().getName());
			assertEquals("service-engine", jbi.getComponent().getType());
			assertEquals("LightWeight Container", jbi.getComponent()
					.getIdentification().getDescription());
			Assets assets = null;
			for (Element element : jbi.getComponent().getAnyOrAny()) {				
				if (("http://servicemix.apache.org/component/packaging"
						.equals(element.getNamespaceURI()))
						&& ("assets".equals(element.getLocalName()))) {
					context = JAXBContext.newInstance(Components.class
							.getPackage().getName());
					Unmarshaller m2 = context.createUnmarshaller();
					assets = (Assets) m2.unmarshal(element);
				}
			}

			assertNotNull(assets);
			assertNotNull(assets.getEngine());
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
