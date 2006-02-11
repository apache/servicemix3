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
