package org.apache.servicemix.descriptors.deployment.assets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.servicemix.descriptors.deployment.assets.Components.Component;

public class TestDeploymentMarshalling extends TestCase {

	public void testComponentXml() {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Components.class.getPackage()
					.getName());
			Unmarshaller m = context.createUnmarshaller();
			Components components = (Components) m.unmarshal(getClass()
					.getClassLoader().getResourceAsStream("components.xml"));
			System.out.println("Components :" + components);
			assertNotNull(components.getComponent());
			assertEquals(1,components.getComponent().size());
			Component component = components.getComponent().get(0);
			assertEquals("servicemix-lwcontainer",component.getName());
			assertNotNull(component.getAssets().getDeploymentAssistants());
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
	}
}
