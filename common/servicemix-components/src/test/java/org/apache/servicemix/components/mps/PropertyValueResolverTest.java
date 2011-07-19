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

import java.io.StringReader;

import javax.jbi.JBIException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Test cases for the PropertyValueResolver class
 */
public class PropertyValueResolverTest extends TestCase {

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	private final static String TEST_STRING = "PROP_TEST_STRING";

	private final static String PROPNAME = "property1";

	private final static String SAMPLE_MSG_XML = "<sample><get x='911'>me</get></sample>";

	private final static String XML_EXISTING_PROP = "<existing-property/>";

	private final static String XML_EXISTING_PROP_OTHER_PROP = "<existing-property name=\"newname\"/>";

	private final static String XML_STATICVAL = "<static-value><![CDATA[a value in the raw]]></static-value>";

	private final static String XML_XPATH = "<xpath-expression><![CDATA[//get[@x='911']]]></xpath-expression>";

	private Document makeDocument(String xml) {
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return db.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * helper method to return a new JBI NormalizedMessage when we need one
	 * 
	 * @return
	 */
	private NormalizedMessage makeTestMessage(String xml) {
		NormalizedMessage message = new NormalizedMessageImpl();
		message.setProperty(PROPNAME, TEST_STRING);
		if (xml == null) {
			xml = "<this><is><some attr='1234'>xml123</some></is></this>";
		}
		try {
			DocumentBuilder db;
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document dom = db.parse(new InputSource(new StringReader(xml)));
			message.setContent(new DOMSource(dom));
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		return message;
	}

	/**
	 * Test that a static string, does return a static string
	 * 
	 */
	public void testStaticStringAsFirst() {
		String propertyNode = (new StringBuffer("<").append(
				PropertyValueResolver.XML_ELEMENT_NAME).append(
				" name='newproperty'>").append(XML_STATICVAL).append("</")
				.append(PropertyValueResolver.XML_ELEMENT_NAME).append(">"))
				.toString();

		NormalizedMessage in = makeTestMessage(SAMPLE_MSG_XML);
		NormalizedMessage out = makeTestMessage(SAMPLE_MSG_XML);
		Document doc = makeDocument(propertyNode);
		try {
			PropertyValueResolver pvr = new PropertyValueResolver(
					"newproperty", doc.getDocumentElement());
			pvr.setProperty(in, out);
			logger.debug("prop = {}", out.getProperty("newproperty"));
			assertTrue(out.getProperty("newproperty").toString().equals(
					"a value in the raw"));

		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		} catch (ConfigNotSupportedException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test the xpath PVR
	 * 
	 */
	public void testXPath() {
		String propertyNode = (new StringBuffer("<").append(
				PropertyValueResolver.XML_ELEMENT_NAME).append(
				" name='newproperty'>").append(XML_XPATH).append(XML_STATICVAL)
				.append("</").append(PropertyValueResolver.XML_ELEMENT_NAME)
				.append(">")).toString();

		NormalizedMessage in = makeTestMessage(PropertyValueResolverTest.SAMPLE_MSG_XML);
		NormalizedMessage out = makeTestMessage(SAMPLE_MSG_XML);
		Document doc = makeDocument(propertyNode);
		try {
			PropertyValueResolver pvr = new PropertyValueResolver(
					"newproperty", doc.getDocumentElement());
			pvr.setProperty(in, out);
			logger.debug("xpath:@newproperty = {}", out.getProperty("newproperty"));
			assertTrue(out.getProperty("newproperty") != null);
			assertTrue(out.getProperty("newproperty").toString().equals("me"));

		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		} catch (ConfigNotSupportedException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test that a existing copier thingo
	 * 
	 */
	public void testExistCopier() {
		String propertyNode = (new StringBuffer("<").append(
				PropertyValueResolver.XML_ELEMENT_NAME).append(
				" name='prop.xyz'>").append(XML_EXISTING_PROP).append(
				XML_STATICVAL).append("</").append(
				PropertyValueResolver.XML_ELEMENT_NAME).append(">")).toString();

		NormalizedMessage in = makeTestMessage(PropertyValueResolverTest.SAMPLE_MSG_XML);
		in.setProperty("prop.xyz", "ahahaha");
		NormalizedMessage out = makeTestMessage(SAMPLE_MSG_XML);
		Document doc = makeDocument(propertyNode);
		try {
			PropertyValueResolver pvr = new PropertyValueResolver("prop.xyz",
					doc.getDocumentElement());
			pvr.setProperty(in, out);
			logger.debug("prop = {}", out.getProperty("prop.xyz"));
			assertTrue(out.getProperty("prop.xyz") != null);
			assertTrue(out.getProperty("prop.xyz").toString().equals("ahahaha"));

		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		} catch (ConfigNotSupportedException e) {
			fail(e.getLocalizedMessage());
		}
	}

}
