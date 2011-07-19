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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.objects.XObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Sets properties on the message, loaded from an XML MPS file 
 * where the properties to set are located in a <property-set ..>
 * inside the XML config file.
 * 
 * There can be more than one propertySet to "load".
 *
 *  The property values are derived from 3 types of config.
 *  The first config that can return a value as a String to
 *  set onto the message, will be the "value" that is set
 *  as the property.
 *  
 *  <static-value>
 *  		As it's name suggests, the "value" of this element
 *          will be the value of the JBI property.
 *          
 *          This is helpful as a default value, or as a static value.
 *          
 *  <exisiting-property> and <existint-property name="..."/>
 *    		This will obtain the value of an existing property (itself)
 *          or another property on the same message.
 *          
 *           This can be helpful when you want the to "ONLY" change the
 *           the value of the property if there is some "xpath" expression
 *           that could not be derived. 
 *           
 *           name=".." form will copy the string value of the other JBI property
 *           onto this one, (duping it). This may be handy when you have a component 
 *           which expects a new property, but you have it as a different name at the moment.
 *           
 *  <xpath-expression>
 *           As it's name suggests, will locate a value in the inMessage source
 *           and set the resulting XPath String as the value of the JBI property.
 *           
 *  So given the three types, they can be arranged in any order. and the first
 *  PropertyValue Type that returns a value, will become the "value" of the JBI property.
 *  
 *
 * <!-- 
 * <mps>
 *	 <property-set name="someSetNameForASetOfProperties">
 *	     <property name="some.property.name1">
 *	         <static-value><![CDATA[value for the property]]></static-value>
 *	     </property>
 *	     <property name="some.property.name2">
 *	         <xpath-expression>
 *		 	      <![CDATA[/someexpath/statement/to/be/applied/to/message/source]]>
 *		 	 </xpath-expression>
 *	         <existing-property name="someproperty"/>
 *		 	 <static-value><![CDATA[a value in the raw]]></static-value>
 *	     </property>
 *	     <property name="prop.xpath.with.static.default">
 *	        <xpath-expression>
 *		 	    <![CDATA[/someexpath/statement]]>
 *		 	</xpath-expression>
 *		 	<static-value><![CDATA[some default if xpath does not resolve]]></static-value>
 *	     </property>
 *	     <property name="prop.xpath.or.keep.existing">
 *	        <xpath-expression>
 *		 	    <![CDATA[/someexpath/statement]]>
 *		 	</xpath-expression>
 *		 	<existing-property/>
 *	     </property>
 *	     <property name="new.prop.name">
 *		 	<existing-property name="other.property"/>
 *	     </property>
 *       <property name="..."> 
 *              ...
 *       </property>
 *	 </property-set>
 *   <property-set name="...">
 *        ...
 *   </property-set>
 * </mps>
 * -->
 *
 */
public class MessagePropertySetterXML extends TransformComponentSupport  {

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The name of our JBI property we may have to interrogate to 
	 * use as the "xpath" for the set of config.
	 */
	public static final String MPS_PROP_NAME_PROPERTYSET = "org.apache.servicemix.components.mps.propertyset";
	
	/**
	 * The XML Element name "property-set"
	 */
	public static final String XML_ELEMENT_NAME = "property-set";
	
	/**
	 * This is our XML file
	 */
	private Resource xmlConfiguration = null;
	
	/**
	 * Our XML config MPS.
	 */
	private Document xmlMPSdom = null;
	
	/**
	 * Holds the propertyValueResolver objects.
	 * if propertySet is not null, this holds one.
	 * if propertySet is null, this will hold
	 * one for each propertyvalue entry used during the time
	 * we are "active".
	 */
	private Map propertSets = new HashMap();

	/**
	 * If this is set, it is hardcoded as the fixed value.
	 * 
	 * If this is null 
	 * we will look for a propery MPS_PROP_NAME_PROPERTYSET
	 * as the "name" of the property set we will work with.
	 * So the message incoming, seeds the set of properties to load, just by
	 * the JBI property itself.
	 */
	private String propertySet = null;
	
	
	/**
	 * If the XPath for a propertySet is not null
	 * we will try and locate the propertySet to use by 
	 * pulling a String from the inMessage content using this
	 * XPath.
	 */
	private String xpathForPropertySet = null;
	
	/**
	 * Here is the transform of the message
	 * We will locate the propertySetName to use 
	 * and the load up our propertySet magic wand and apply all the properties
	 * we can to the outgoing NormalizedMessage
	 */
	protected boolean transform(MessageExchange arg0, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
		try {
			copyPropertiesAndAttachments(arg0,in,out);
			out.setContent(in.getContent());
			
			String propertySetName = "";
			if (xpathForPropertySet != null) {
				try {
					CachedXPathAPI xpathApi = new org.apache.xpath.CachedXPathAPI();
					Document doc = new SourceTransformer().toDOMDocument(in);
					XObject propSetXO = xpathApi.eval(doc.getDocumentElement(),xpathForPropertySet);
					propertySetName = propSetXO.str();
				} catch (Exception e) {
					throw new MessagingException("Problem getting the propertySet using XPath", e);
				}
			} else if (this.propertySet != null) {
				propertySetName = this.propertySet;
			} else if (in.getProperty(MPS_PROP_NAME_PROPERTYSET) != null) {
				propertySetName = in.getProperty(MPS_PROP_NAME_PROPERTYSET).toString();
			} else {
				return false;
			}
			logger.info("Applying properties from property-set [" + propertySetName + "]");
			getPropertySetByName(propertySetName).applyProperties(in,out);
			return true;				
		} catch (JBIException e) {
			throw new MessagingException("Problem setting properties",e);
		} catch (PropertySetNotFoundException e) {
			logger.warn(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * Initialise by loading the mps.xml file up into an internal DOM.
	 * 
	 * @throws JBIException
	 */
	private void initConfig() throws JBIException {
		Assert.notNull(this.xmlConfiguration);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setIgnoringElementContentWhitespace(true);
		domFactory.setIgnoringComments(true);
		domFactory.setCoalescing(true); // convert CDATA to test nodes
        DocumentBuilder domBuilder;
		try {
			logger.info("Intialising MessagePropertySetterXML, loading settings from " + this.xmlConfiguration.getFile().getAbsolutePath());
			domBuilder = domFactory.newDocumentBuilder();
			xmlMPSdom = domBuilder.parse(this.xmlConfiguration.getInputStream());
		} catch (ParserConfigurationException e) {
			throw new JBIException("Problem parsing the XML config file for MPS",e);
		} catch (SAXException e) {
			throw new JBIException("Problem loading the XML config file for MPS",e);
		} catch (IOException e) {
			throw new JBIException("Problem loading the XML config file for MPS",e);
		}
		
	}

	/**
	 * We are only interested  in loading the XML onfig, nothing else
	 */
	public void init(ComponentContext context) throws JBIException {
		super.init(context);
		initConfig();
	}

	/**
	 * Create a propertyset (loading up the Object config based on the XML config)
	 * 
	 * @param propertySetName
	 * @return
	 * @throws JBIException
	 * @throws PropertySetNotFoundException
	 */
	private PropertySet createPropertySet(String propertySetName) throws JBIException , PropertySetNotFoundException{
		PropertySet ps;	
		CachedXPathAPI xpath = new CachedXPathAPI();
		StringBuffer xpathSB = new StringBuffer("//")
									.append(XML_ELEMENT_NAME)
									.append("[@name='")
									.append(propertySetName)
									.append("']");
		try {
			Node propertySetNode = xpath.selectSingleNode(xmlMPSdom,xpathSB.toString());
			if (propertySetNode == null) {
				throw new PropertySetNotFoundException("Could not find a property-set for [" + propertySetName + "] in " + xmlConfiguration.getFilename());
			}
			ps = new PropertySet(propertySetName,(Element) propertySetNode);
			this.propertSets.put(propertySetName, ps);
		} catch (TransformerException e) {
			throw new JBIException("Could not load the PropertySet for " + propertySet,e);		
		} catch (ConfigNotSupportedException e) {
			throw new JBIException("Could not load the PropertySet for. XMLConfig is not good for " + propertySet,e);		
		}
		return ps;

	}
	
	/**
	 * Get a property set from ou
	 * @param name
	 * @return
	 * @throws JBIException 
	 */
	private PropertySet getPropertySetByName(String name) throws JBIException, PropertySetNotFoundException {
		// find a pre "created" one
		if (this.propertSets.containsKey(name)) {
			return (PropertySet)this.propertSets.get(name);
		} else {
			return this.createPropertySet(name);
		}
		
	}

	/**
	 * @param propertySet The propertySet to set.
	 */
	public void setPropertySet(String propertySet) {
		this.propertySet = propertySet;
	}

	/**
	 * @param xmlConfiguration The xmlConfiguration to set.
	 */
	public void setXmlConfiguration(Resource xmlConfiguration) {
		this.xmlConfiguration = xmlConfiguration;
	}

	/**
	 * @param xpathForPropertySet xpath to apply to a message to derive the name of the property-set we want to load
	 */
	public void setXpathForPropertySet(String xpathForPropertySet) {
		this.xpathForPropertySet = xpathForPropertySet;
	}

}
