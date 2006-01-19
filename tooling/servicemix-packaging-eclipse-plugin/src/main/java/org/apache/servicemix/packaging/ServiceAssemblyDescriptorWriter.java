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
package org.apache.servicemix.packaging;

import java.io.Writer;

import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * The Service Assembly descriptor writer
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class ServiceAssemblyDescriptorWriter {

	public void write(Writer w, ServiceAssembly assembly) {
		XMLWriter writer = new PrettyPrintXMLWriter(w, "UTF-8", null);
		writer.startElement("jbi");
		writer.addAttribute("xmlns", "http://java.sun.com/xml/ns/jbi");
		writer.addAttribute("version", "1.0");

		writer.startElement("service-assembly");
		writer.startElement("identification");

		writer.startElement("name");
		writer.writeText(assembly.getName());
		writer.endElement();

		writer.startElement("description");
		// writer.writeText(assembly.getDescription());
		writer.endElement();

		for (ServiceUnit unit : assembly.getServiceUnit()) {
			writer.startElement("service-unit");
			writer.startElement("identification");
			writer.startElement("name");
			writer.addAttribute("xmlns:ns1", unit.getServiceName()
					.getNamespaceURI());
			writer.writeText("ns1:" + unit.getServiceName().getLocalPart());
			writer.endElement();
			writer.startElement("description");
			// writer.writeText(unit.getDescription());
			writer.endElement();
			writer.endElement();

			writer.startElement("target");
			writer.startElement("artifacts-zip");
			writer.writeText(unit.getServiceName().getLocalPart() + ".zip");
			writer.endElement();

			writer.startElement("component-name");
			writer.writeText(assembly.getName());
			writer.endElement();

			writer.endElement();
			writer.endElement();
		}

		writer.endElement();
		writer.endElement();
		writer.endElement();
	}
}
