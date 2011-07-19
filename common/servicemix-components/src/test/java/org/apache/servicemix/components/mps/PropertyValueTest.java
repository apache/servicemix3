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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Test cases for the Property Value type Objects
 */
public class PropertyValueTest extends TestCase {

	private final static String TEST_STRING = "PROP_TEST_STRING";
	private final static String PROPNAME = "property1";

	/**
	 * helper method to return a new JBI NormalizedMessage
	 * when we need one
	 * @return
	 */
	private NormalizedMessage getTestMessage() {
		NormalizedMessage message = new NormalizedMessageImpl();
		message.setProperty(PROPNAME,TEST_STRING);
		String xml = "<this><is><some attr='1234'>xml123</some></is></this>";
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
	public void testStaticString() {
		PropertyValue pv = new StaticStringPropertyValue("someValueX");
		try {
			assertTrue(pv.getPropertyValue(getTestMessage()).equals("someValueX"));
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test that a existing property copier works
	 *
	 */
	public void testExistingCopier() {
		PropertyValue pv = new ExistingPropertyCopier(PROPNAME);
		try {
			assertTrue(pv.getPropertyValue(getTestMessage()).equals(TEST_STRING));
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test that a existing property copier works
	 *
	 */
	public void testXPathElementPropertyValue() {
		PropertyValue pv = new XPathContentMessagePropertyValue("/this/is/some");
		try {
			assertTrue(pv.getPropertyValue(getTestMessage()).equals("xml123"));
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test that a existing property copier works
	 *
	 */
	public void testXPathAttrPropertyValue() {
		PropertyValue pv = new XPathContentMessagePropertyValue("/this/is/some/@attr");
		try {
			assertTrue(pv.getPropertyValue(getTestMessage()).equals("1234"));
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test that a existing property copier works
	 *
	 */
	public void testXPathConcatPropertyValue() {
		PropertyValue pv = new XPathContentMessagePropertyValue("concat('x','y')");
		try {
			assertTrue(pv.getPropertyValue(getTestMessage()).equals("xy"));
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());
		}
	}

}
