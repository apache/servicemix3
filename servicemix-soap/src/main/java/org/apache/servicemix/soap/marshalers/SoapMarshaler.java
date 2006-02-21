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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.servicemix.jbi.jaxp.StAXSourceTransformer;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 359186 $
 * @since 3.0 
 */
public class SoapMarshaler {

	public static final String MULTIPART_CONTENT = "multipart/related";
	public static final String SOAP_PART_ID = "soap-request";
	public static final String SOAP_11_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String SOAP_12_URI = "http://www.w3.org/2003/05/soap-envelope";
	public static final String SOAP_PREFIX = "env";
	public static final String ENVELOPE = "Envelope";
	public static final String HEADER = "Header";
	public static final String BODY = "Body";
	public static final String FAULT = "Fault";

	protected XMLInputFactory inputFactory;
	protected XMLOutputFactory outputFactory;
	protected StAXSourceTransformer  sourceTransformer;
	protected boolean repairingNamespace;
	protected String prefix = SOAP_PREFIX;
	protected boolean soap = true;
	protected String soapUri = SOAP_12_URI;

	public SoapMarshaler() {
	}

	public SoapMarshaler(boolean soap) {
		this.soap = soap;
	}

    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = XMLInputFactory.newInstance();
            inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        }
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = XMLOutputFactory.newInstance();
            if (isRepairingNamespace()) {
                outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, 
                						  Boolean.valueOf(isRepairingNamespace()));
            }
        }
        return outputFactory;
    }
    
    public StAXSourceTransformer getSourceTransformer() {
    	if (sourceTransformer == null) {
    		sourceTransformer = new StAXSourceTransformer();
    	}
    	return sourceTransformer;
    }

    public boolean isRepairingNamespace() {
        return repairingNamespace;
    }

    public void setRepairingNamespace(boolean repairingNamespace) {
        this.repairingNamespace = repairingNamespace;
    }
    
    public boolean isSoap() {
    	return soap;
    }
    
    public void setSoap(boolean soap) {
    	this.soap = soap;
    }
    
    public String getPrefix() {
    	return prefix;
    }
    
    public void setPrefix(String prefix) {
    	this.prefix = prefix;
    }
    
    public String getSoapUri() {
    	return soapUri;
    }
    
    public void setSoapUri(String soapUri) {
    	this.soapUri = soapUri;
    }
    
    public SoapReader createReader() {
    	return new SoapReader(this);
    }

	public SoapWriter createWriter(SoapMessage message) {
		return new SoapWriter(this, message);
	}
}
