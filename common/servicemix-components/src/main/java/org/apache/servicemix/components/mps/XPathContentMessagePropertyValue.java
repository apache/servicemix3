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
package org.apache.servicemix.components.mps;

import javax.jbi.JBIException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.w3c.dom.Document;

import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.objects.XObject;

/**
 * Get the Value of a property from the message given the XPath Statement.
 */
public class XPathContentMessagePropertyValue implements PropertyValue {
	
	public final static String XML_ELEMENT_NAME = "xpath-expression";
	
	private String xpath;
	
	public XPathContentMessagePropertyValue(String xpath) {
		this.xpath = xpath;
	}

	/**
	 * Get a value give the XPath statement preset in our Ctor.
	 * Return null if the value was empty or not there
	 */
	public String getPropertyValue(NormalizedMessage msg) throws JBIException {
		String resultValue = null;
		if (msg.getContent() != null) {
			CachedXPathAPI xpathApi = new CachedXPathAPI();
			try {
				Document doc = new SourceTransformer().toDOMDocument(msg);
				XObject result = xpathApi.eval(doc,xpath);

				resultValue = result.toString();
				if ("".equals(resultValue)) {
					resultValue = null;
				}
			} catch (Exception e) {
				throw new JBIException("Could not get value from message via xpath " + xpath, e);
			}
		} 
		return resultValue;
	}
	
}
