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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.JbiConstants;
import org.w3c.dom.DocumentFragment;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 * @since 3.0
 */
public class JBIMarshaler {

	public void toNMS(NormalizedMessage normalizedMessage, SoapMessage soapMessage) throws Exception {
    	if (soapMessage.hasHeaders()) {
    		normalizedMessage.setProperty(JbiConstants.SOAP_HEADERS, soapMessage.getHeaders());
    	}
        if (soapMessage.hasAttachments()) {
        	Map attachments = soapMessage.getAttachments();
        	for (Iterator it = attachments.entrySet().iterator(); it.hasNext();) {
        		Map.Entry entry = (Map.Entry) it.next();
        		normalizedMessage.addAttachment((String) entry.getKey(), 
        										(DataHandler) entry.getValue());
        	}
        }
        normalizedMessage.setContent(soapMessage.getSource());
	}
	
	public void fromNMS(SoapMessage soapMessage, NormalizedMessage normalizedMessage) {
		if (normalizedMessage.getProperty(JbiConstants.SOAP_HEADERS) != null) {
			Map headers = (Map) normalizedMessage.getProperty(JbiConstants.SOAP_HEADERS);
        	for (Iterator it = headers.entrySet().iterator(); it.hasNext();) {
        		Map.Entry entry = (Map.Entry) it.next();
        		soapMessage.addHeader((QName) entry.getKey(), (DocumentFragment) entry.getValue());
        	}
		}
		Set attachmentNames = normalizedMessage.getAttachmentNames();
		for (Iterator it = attachmentNames.iterator(); it.hasNext();) {
			String id = (String) it.next();
			DataHandler handler = normalizedMessage.getAttachment(id);
			soapMessage.addAttachment(id, handler);
		}
		soapMessage.setSource(normalizedMessage.getContent());
	}

}
