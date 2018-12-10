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
package org.apache.servicemix.maven.plugin.jbi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

public class JbiComponentDescriptorWriter {

	private final String encoding;

	public JbiComponentDescriptorWriter(String encoding) {
		this.encoding = encoding;
	}

	public void write(File descriptor, 
					  String component, 
					  String bootstrap, 
					  String type,
					  String name,
					  String description,
					  List uris)
			throws JbiPluginException {
		FileWriter w;
		try {
			w = new FileWriter(descriptor);
		} catch (IOException ex) {
			throw new JbiPluginException("Exception while opening file["
					+ descriptor.getAbsolutePath() + "]", ex);
		}

		XMLWriter writer = new PrettyPrintXMLWriter(w, encoding, null);
		writer.startElement("jbi");
		writer.addAttribute("xmlns", "http://java.sun.com/xml/ns/jbi");
		writer.addAttribute("version", "1.0");

		writer.startElement("component");
		writer.addAttribute("type", type);
		
		writer.startElement("identification");
		writer.startElement("name");
		writer.writeText(name);
		writer.endElement();
		writer.startElement("description");
		writer.writeText(description);
		writer.endElement();
		writer.endElement();
		
		writer.startElement("component-class-name");
		writer.writeText(component);
		writer.endElement();
		writer.startElement("component-class-path");
		for (Iterator it = uris.iterator(); it.hasNext();) {
			writer.startElement("path-element");
			writer.writeText(it.next().toString());
			writer.endElement();
		}
		writer.endElement();

		writer.startElement("bootstrap-class-name");
		writer.writeText(bootstrap);
		writer.endElement();
		writer.startElement("bootstrap-class-path");
		for (Iterator it = uris.iterator(); it.hasNext();) {
			writer.startElement("path-element");
			writer.writeText(it.next().toString());
			writer.endElement();
		}
		writer.endElement();
		
		writer.endElement();
		
		writer.endElement();

		close(w);
	}
	
	private void close(Writer closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				// TODO: warn
			}
		}
	}

}
