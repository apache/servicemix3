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

public class PropertySetTest extends TestCase {

	private final static String TEST_STRING = "PROP_TEST_STRING";
	private final static String PROPNAME = "property1";
	private final static String SAMPLE_MSG_XML = "<sample><get x='911'>me</get></sample>";
	private final static String SAMPLE_MSG_XML2 = "<this><is><some attr='1234'>xml123</some></is></this>";
	
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
	 * helper method to return a new JBI NormalizedMessage
	 * when we need one
	 * @return
	 */
	private NormalizedMessage makeTestMessage(String xml) {
		NormalizedMessage message = new NormalizedMessageImpl();
		message.setProperty(PROPNAME,TEST_STRING);
		if (xml == null) {
		    xml = SAMPLE_MSG_XML2;
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
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testSimpleStaticValue() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='testSimpleStaticValue.property'>")
			                    .append("<existing-property name='property1'/>")
			                    .append("</property>")
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage(null);
			NormalizedMessage outMessage = makeTestMessage(null);
			ps.applyProperties(inMessage,outMessage);
			
			assertTrue(TEST_STRING.equals(outMessage.getProperty("testSimpleStaticValue.property")));
		} catch (ConfigNotSupportedException e) {
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testSimpleExistingPropInvalidXPath() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='testSimpleExistingPropInvalidXPath.property'>")
			                    .append("<xpath-expression><![CDATA[/someexpath/statement]]></xpath-expression>")
			                    .append("<existing-property name='property1'/>")
			                    .append("</property>")
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage(null);
			NormalizedMessage outMessage = makeTestMessage(null);
			ps.applyProperties(inMessage,outMessage);
			
			assertTrue(TEST_STRING.equals(outMessage.getProperty("testSimpleExistingPropInvalidXPath.property")));
		} catch (ConfigNotSupportedException e) {
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testStaticValueDefaultWithBadXpath() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='testStaticValueDefaultWithBadXpath.property'>")
			                    .append("<xpath-expression><![CDATA[/someexpath/statement]]></xpath-expression>")
			                    .append("<static-value>myvalue</static-value>")
			                    .append("</property>")
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage(null);
			NormalizedMessage outMessage = makeTestMessage(null);
			ps.applyProperties(inMessage,outMessage);
			
			assertTrue("myvalue".equals(outMessage.getProperty("testStaticValueDefaultWithBadXpath.property")));
		} catch (ConfigNotSupportedException e) {
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			fail(e.getLocalizedMessage());// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	/**
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testSimpleValidXPath() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='testSimpleValidXPath.property'>")
			                    .append("<xpath-expression><![CDATA[/this//some/@attr]]></xpath-expression>")
			                    .append("<existing-property name='property1'/>")
			                    .append("</property>")
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage("<this><is><some attr='1234'>xml123</some></is></this>");
			NormalizedMessage outMessage = makeTestMessage(null);
			ps.applyProperties(inMessage,outMessage);
			
			assertTrue(outMessage.getProperty("testSimpleValidXPath.property").toString().equals("1234"));
		} catch (ConfigNotSupportedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}	
	
	
	/**
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testInvalidXPathSingle() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='testInvalidXPathSingle.property'>")
			                    .append("<xpath-expression><![CDATA[/NoNode]]></xpath-expression>")
			                    .append("</property>")
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage("<this><is><some attr='1234'>xml123</some></is></this>");
			NormalizedMessage outMessage = makeTestMessage(null);
			ps.applyProperties(inMessage,outMessage);
			
			assertTrue("Node is not null",outMessage.getProperty("testInvalidXPathSingle.property") == null);
		} catch (ConfigNotSupportedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testXPATH2MessagesOneHasOneHasnt() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='testXPATH2MessagesOneHasOneHasnt.property'>")
			                    .append("<xpath-expression><![CDATA[/this//some/@attr]]></xpath-expression>")
			                    .append("<existing-property name='property1'/>")
			                    .append("</property>")
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage("<this><is><some attr='1234'>xml123</some></is></this>");
			// inMessage will have a property1=TEST_STRING
			
			NormalizedMessage inMessage2 = makeTestMessage("<this><is><some>xml123</some></is></this>");
			// inMessage2 will have a property1=TEST_STRING			
			NormalizedMessage outMessage = makeTestMessage(null);

			ps.applyProperties(inMessage,outMessage);
			assertTrue(outMessage.getProperty("testXPATH2MessagesOneHasOneHasnt.property").toString().equals("1234"));

			ps.applyProperties(inMessage2,outMessage);
			assertTrue(outMessage.getProperty("testXPATH2MessagesOneHasOneHasnt.property").toString().equals(TEST_STRING));

		} catch (ConfigNotSupportedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}	
	
	/**
	 * Test 
	 * <property-set name="somename">
	 *    <property name="newProperty">
	 *    	<existing-property name="property1"/>
	 *    </property>
	 * </property-set>
	 *
	 */
	public void testManyProperties() {
		String xmlConfig = new StringBuffer("<property-set name='somename'>")
			                    .append("<property name='newProperty1'>")
			                       .append("<xpath-expression><![CDATA[/this//some/@attr]]></xpath-expression>")
			                       .append("<existing-property name='property1'/>")
			                    .append("</property>")
			                    .append("<property name='newProperty2'>")
			                       .append("<xpath-expression><![CDATA[/this//some/@attrNoExisting]]></xpath-expression>")
			                       .append("<static-value><![CDATA[Some String I expect to be Set]]></static-value>")
			                    .append("</property>")
			                    .append("<property name='newProperty3'>")
			                       .append("<existing-property name='missingProperty'/>")
			                       .append("<existing-property name='missingProperty2'/>")
			                       .append("<static-value><![CDATA[Some String I expect to be Set]]></static-value>")
			                    .append("</property>")
			                    .append("<property name='newProperty4'>")
			                       .append("<xpath-expression><![CDATA[/this//some/nothere/@attr]]></xpath-expression>")
			                       .append("<existing-property name='property1WhichIsNoteThere'/>")
			                       .append("<existing-property name='testProperty'/>")
			                    .append("</property>")
			                    .append("<property name='newProperty5'>")
			                       .append("<static-value><![CDATA[tagged]]></static-value>")
			                       .append("<static-value><![CDATA[taggedNotSet]]></static-value>")
			                    .append("</property>")
			                    .append("<property name='newProperty6'>")
			                       .append("<xpath-expression><![CDATA[/this//some/@attrNoExisting]]></xpath-expression>")
			                       .append("</property>")			                    
			                    .append("</property-set>").toString();
		Document propertySet = makeDocument(xmlConfig);
		try {
			PropertySet ps = new PropertySet("somename",propertySet.getDocumentElement());
			NormalizedMessage inMessage = makeTestMessage("<this><is><some attr='1234'>xml123</some></is></this>");
			inMessage.setProperty("testProperty","value123");
			NormalizedMessage outMessage = makeTestMessage(null);
			ps.applyProperties(inMessage,outMessage);
			
			assertTrue("newProperty1 was not set to expected value",outMessage.getProperty("newProperty1").toString().equals("1234"));
			assertTrue("newProperty2 was not set to expected value",outMessage.getProperty("newProperty2").toString().equals("Some String I expect to be Set"));
			assertTrue("newProperty3 was not set to expected value",outMessage.getProperty("newProperty3").toString().equals("Some String I expect to be Set"));
			assertTrue("newProperty4 was not set to expected value",outMessage.getProperty("newProperty4").toString().equals("value123"));
			assertTrue("newProperty5 was not set to expected value",outMessage.getProperty("newProperty5").toString().equals("tagged"));
			assertTrue("newProperty5 was not set to expected value",outMessage.getProperty("newProperty6") == null);
		} catch (ConfigNotSupportedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JBIException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}	
	
}
