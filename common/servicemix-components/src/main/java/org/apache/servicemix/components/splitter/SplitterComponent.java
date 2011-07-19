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
package org.apache.servicemix.components.splitter;

import java.io.IOException;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.servicemix.jbi.MissingPropertyException;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This Component splits a message according to a XPath expression.
 *
 * @deprecated use the XPathSplitter pattern from the EIP component instead
 */
public class SplitterComponent extends TransformComponentSupport {

	/** Holds value of property nodePath. */
	private String nodePath;

	private XPathExpression expression;

	private SourceTransformer st = new SourceTransformer();

	protected void init() throws JBIException {
		super.init();

		if (nodePath == null) {
			throw new MissingPropertyException("nodePath");
		}
	}

	protected boolean transform(MessageExchange me, NormalizedMessage in,
			NormalizedMessage out) throws MessagingException {
		NodeList nodes;
		try {
			Node doc = st.toDOMNode(in);
			if (expression == null) {
				XPath xpath = XPathFactory.newInstance().newXPath();
				expression = xpath.compile(nodePath);
			}
			nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
		} catch (TransformerException e) {
			throw new MessagingException(e);
		} catch (IOException e) {
			throw new MessagingException(e);
		} catch (SAXException e) {
			throw new MessagingException(e);
		} catch (ParserConfigurationException e) {
			throw new MessagingException(e);
		} catch (XPathExpressionException e) {
			// If XPath Expression is mal formed
			throw new MessagingException(e);
		}
		int total = nodes.getLength();
		for (int i = 0; i < total; i++) {
			out.setContent(new DOMSource(nodes.item(i)));
			InOnly outExchange = getExchangeFactory().createInOnlyExchange();
			outExchange.setInMessage(out);
			getDeliveryChannel().sendSync(outExchange);
			outExchange.setStatus(ExchangeStatus.DONE);
		}
		return false;
	}

	/**
	 * Getter for property nodePath.
	 * 
	 * @return Value of property nodePath.
	 */
	public String getNodePath() {
		return nodePath;
	}

	/**
	 * @org.xbean.Property alias="select"
	 * 
	 * Setter for property nodePath.
	 * @param nodePath
	 *            New value of property nodePath.
	 */
	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
		this.expression = null;
	}

}
