/***************************************************************
 * Unity Information Platform
 * 
 * (C) Copyright 2003, 2005 Unity Systems (http://www.unity-systems.com)
 * All Rights Reserved
 * 
 * This code is protected by copyright law and international treaties.
 * Unauthorized reproduction or distribution of this code, or any portion of it,
 * may result in severe civil and criminal penalties, and will be prosecuted to 
 * the maximum extent possible under the law.
 ****************************************************************/
package org.apache.servicemix.packaging.descriptor;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class TestDescriptorMarshalling extends TestCase {

	private Components components;

	public void testUnmarshall() throws JAXBException {
		JAXBContext context;

		context = JAXBContext.newInstance(Components.class.getPackage()
				.getName());
		Unmarshaller m = context.createUnmarshaller();
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("components.xml");
		assertNotNull(inputStream);
		components = (Components) m.unmarshal(inputStream);

		assertNotNull(components);
		assertNotNull(components.getComponent().get(0));
		Component component = components.getComponent().get(0);
		assertEquals("Provider", component.getName());
		assertEquals("40bcb7a0-6c3f-11da-bcff-0002a5d5c51b", component
				.getComponentUuid());
		assertNotNull(component.getAssets());
		assertEquals("binding-component", component.getType());
		assertEquals(1, component.getAssets().getParameter().size());
		assertEquals(1, component.getAssets().getConnection().size());
		assertEquals("destinationService", component.getAssets()
				.getConnection().get(0).getName());

		assertNotNull(component.getServiceUnit());
		assertNotNull(component.getServiceUnit().getAssets());
		assertNotNull(component.getServiceUnit().getAssets()
				.getEmbeddedArtifact().get(0));
		assertEquals("Service Unit Definition", component.getServiceUnit()
				.getAssets().getEmbeddedArtifact().get(0).getDescription());
		System.out.println(components);
	}

	public void testMarshall() throws JAXBException {
		JAXBContext context;

		components = new Components();

		Component component = new Component();

		component.setDescription("Example");
		component.setAssets(new Assets());
		Parameter parameter = new Parameter();
		parameter.setDefaultValue("defaultValue");
		component.getAssets().getParameter().add(parameter);
		EmbeddedArtifact artifact = new EmbeddedArtifact();
		artifact.setDescription("sponge");
		artifact.setExtension("cake");
		component.getAssets().getEmbeddedArtifact().add(artifact);

		components.getComponent().add(component);

		context = JAXBContext.newInstance(Components.class.getPackage()
				.getName());
		Marshaller m = context.createMarshaller();
		StringWriter write = new StringWriter();
		m.marshal(components, write);
		System.out.println(write.toString());

	}
}
