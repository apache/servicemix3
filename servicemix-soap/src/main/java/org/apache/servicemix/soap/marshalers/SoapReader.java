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
package org.apache.servicemix.soap.marshalers;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.servicemix.jbi.jaxp.ExtendedXMLStreamReader;
import org.apache.servicemix.jbi.jaxp.FragmentStreamReader;
import org.apache.servicemix.jbi.jaxp.StaxSource;
import org.apache.servicemix.soap.SoapFault;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 * @since 3.0
 */
public class SoapReader {

	private SoapMarshaler marshaler;

	public SoapReader(SoapMarshaler marshaler) {
		this.marshaler = marshaler;
	}

	public SoapMessage read(InputStream is, String contentType)
			throws Exception {
		if (contentType != null && contentType.toLowerCase().indexOf(SoapMarshaler.MULTIPART_CONTENT) != -1) {
			Session session = Session.getDefaultInstance(new Properties());
			MimeMessage mime = new MimeMessage(session, is);
			mime.setHeader("Content-Type", contentType);
			return read(mime);
		} else {
			return read(is);
		}
	}

	public SoapMessage read(InputStream is) throws Exception {
		if (marshaler.isSoap()) {
			return readSoap(is);
		} else {
			SoapMessage message = new SoapMessage();
			message.setSource(new StreamSource(is));
			return message;
		}
	}

	private SoapMessage readSoap(InputStream is) throws Exception {
		SoapMessage message = new SoapMessage();
		XMLStreamReader reader = marshaler.getInputFactory().createXMLStreamReader(is);
		reader = new ExtendedXMLStreamReader(reader);
		reader.nextTag();
		// Check Envelope tag
		if (!reader.getLocalName().equals(SoapMarshaler.ENVELOPE)) {
			throw new SoapFault(SoapFault.SENDER, "Unrecognized element: "
					+ reader.getName() + " at ["
					+ reader.getLocation().getLineNumber() + ","
					+ reader.getLocation().getColumnNumber()
					+ "]. Expecting 'Envelope'.");
		}
		message.setEnvelopeName(reader.getName());
		// Check soap 1.1 or 1.2
		String soapUri = reader.getNamespaceURI();
		if (!SoapMarshaler.SOAP_11_URI.equals(soapUri) && !SoapMarshaler.SOAP_12_URI.equals(soapUri)) {
			throw new SoapFault(SoapFault.SENDER, "Unrecognized namespace: " + soapUri
					+ " for element 'Envelope' at ["
					+ reader.getLocation().getLineNumber() + ","
					+ reader.getLocation().getColumnNumber()
					+ "]. Expecting 'Envelope'.");
		}
		// Check Headers
		reader.nextTag();
		if (reader.getName().equals(new QName(soapUri, SoapMarshaler.HEADER))) {
			parseHeaders(message, reader);
			reader.nextTag();
		}
		// Check Body
		if (!reader.getName().equals(new QName(soapUri, SoapMarshaler.BODY))) {
			throw new SoapFault(SoapFault.SENDER, "Unrecognized element: "
					+ reader.getName() + " at ["
					+ reader.getLocation().getLineNumber() + ","
					+ reader.getLocation().getColumnNumber()
					+ "]. Expecting 'Body'.");
		}
		// Create Source for content
		if (reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
			message.setBodyName(reader.getName());
			message.setSource(new StaxSource(new FragmentStreamReader(reader)));
		}
		return message;
	}

	private void parseHeaders(SoapMessage message, XMLStreamReader reader)
			throws Exception {
		while (reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
			QName hn = reader.getName();
			FragmentStreamReader rh = new FragmentStreamReader(reader);
			Document doc = (Document) marshaler.getSourceTransformer().toDOMNode(
					new StaxSource(rh));
			DocumentFragment df = doc.createDocumentFragment();
			df.appendChild(doc.getDocumentElement());
			message.addHeader(hn, df);
		}
	}

	public SoapMessage read(MimeMessage mime) throws Exception {
		final Object content = mime.getContent();
		if (content instanceof MimeMultipart) {
			MimeMultipart multipart = (MimeMultipart) content;
			ContentType type = new ContentType(mime.getContentType());
			String contentId = type.getParameter("start");
			if (contentId == null) {
				contentId = ((MimeBodyPart) multipart.getBodyPart(0))
						.getContentID();
			}
			// Get request
			MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(contentId);
			SoapMessage message = read(part.getInputStream());
			for (int i = 0; i < multipart.getCount(); i++) {
				part = (MimeBodyPart) multipart.getBodyPart(i);
				String id = part.getContentID();
				if (id != null && !id.equals(contentId)) {
					if (id.startsWith("<")) {
						id = id.substring(1, id.length() - 1);
					}
					message.addAttachment(id, part.getDataHandler());
				}
			}
			return message;
		} else {
			throw new UnsupportedOperationException(
					"Expected a javax.mail.internet.MimeMultipart object");
		}
	}

}
