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
package org.apache.servicemix.jbi.jaxp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.servicemix.jbi.util.FastStack;

/**
 * Abstract logic for creating XMLStreamReader from DOM documents.
 * Its works using adapters for Element, Node and Attribute ( @see ElementAdapter }
 * 
 * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
 */
public abstract class DOMStreamReader implements XMLStreamReader {
	public Map properties = new HashMap();

	private FastStack frames = new FastStack();

	private ElementFrame frame;

	private int currentEvent = XMLStreamConstants.START_DOCUMENT;

	/**
	 *     
	 */
	public static class ElementFrame {
		public ElementFrame(Object element) {
			this.element = element;
		}

		Object element;

		boolean started = false;

		boolean ended = false;

		int currentChild = -1;

		int currentAttribute = -1;

		int currentNamespace = -1;

		List uris;

		List prefixes;

		List attributes;
	}

	/**
	 * @param element
	 */
	public DOMStreamReader(ElementFrame frame) {
		this.frame = frame;
		frames.push(this.frame);
		newFrame(frame);
	}

	protected ElementFrame getCurrentFrame() {
		return frame;
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) throws IllegalArgumentException {
		return properties.get(key);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#next()
	 */
	public int next() throws XMLStreamException {

		if (frame.ended) {
			frames.pop();
			if (!frames.isEmpty()) {
				frame = (ElementFrame) frames.peek();
			} else {
				currentEvent = END_DOCUMENT;
				return currentEvent;
			}
		}

		if (!frame.started) {
			frame.started = true;
			currentEvent = START_ELEMENT;
		} else if (frame.currentAttribute < getAttributeCount() - 1) {
			frame.currentAttribute++;
			currentEvent = ATTRIBUTE;
		} else if (frame.currentNamespace < getNamespaceCount() - 1) {
			frame.currentNamespace++;
			currentEvent = NAMESPACE;
		} else if (frame.currentChild < getChildCount() - 1) {
			frame.currentChild++;

			currentEvent = moveToChild(frame.currentChild);

			if (currentEvent == START_ELEMENT) {
				ElementFrame newFrame = getChildFrame(frame.currentChild);
				newFrame.started = true;
				frame = newFrame;
				frames.push(this.frame);
				currentEvent = START_ELEMENT;

				newFrame(newFrame);
			}
		} else {
			frame.ended = true;
			currentEvent = END_ELEMENT;
			endElement();
		}
		return currentEvent;
	}

	protected void newFrame(ElementFrame newFrame) {
	}

	protected void endElement() {
	}

	protected abstract int moveToChild(int currentChild);

	protected abstract ElementFrame getChildFrame(int currentChild);

	protected abstract int getChildCount();

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#require(int, java.lang.String, java.lang.String)
	 */
	public void require(int arg0, String arg1, String arg2)
			throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#getElementText()
	 */
	public abstract String getElementText() throws XMLStreamException;

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#nextTag()
	 */
	public int nextTag() throws XMLStreamException {
		while (hasNext()) {
			if (START_ELEMENT == next()) {
				return START_ELEMENT;
			}
		}
		return currentEvent;
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#hasNext()
	 */
	public boolean hasNext() throws XMLStreamException {
		return !(frames.size() == 0 && frame.ended);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#close()
	 */
	public void close() throws XMLStreamException {
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#getNamespaceURI(java.lang.String)
	 */
	public abstract String getNamespaceURI(String prefix);

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#isStartElement()
	 */
	public boolean isStartElement() {
		return (currentEvent == START_ELEMENT);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#isEndElement()
	 */
	public boolean isEndElement() {
		return (currentEvent == END_ELEMENT);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#isCharacters()
	 */
	public boolean isCharacters() {
		return (currentEvent == CHARACTERS);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#isWhiteSpace()
	 */
	public boolean isWhiteSpace() {
		return (currentEvent == SPACE);
	}

	public int getEventType() {
		return currentEvent;
	}

	public int getTextCharacters(int sourceStart, char[] target,
			int targetStart, int length) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	public boolean hasText() {
		return (currentEvent == CHARACTERS || currentEvent == DTD
				|| currentEvent == ENTITY_REFERENCE || currentEvent == COMMENT 
				|| currentEvent == SPACE);
	}

	public Location getLocation() {
		return null;
	}

	public boolean hasName() {
		return (currentEvent == START_ELEMENT || currentEvent == END_ELEMENT);
	}

	public String getVersion() {
		return null;
	}

	public boolean isStandalone() {
		return false;
	}

	public boolean standaloneSet() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCharacterEncodingScheme() {
		// TODO Auto-generated method stub
		return null;
	}
}
