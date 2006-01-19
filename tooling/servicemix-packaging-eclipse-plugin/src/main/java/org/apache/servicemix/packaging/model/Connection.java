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
package org.apache.servicemix.packaging.model;

import org.eclipse.draw2d.Graphics;

/**
 * A connection between to connectables
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class Connection extends ModelElement {

	public static final Integer SOLID_CONNECTION = new Integer(
			Graphics.LINE_SOLID);

	public static final Integer DASHED_CONNECTION = new Integer(
			Graphics.LINE_DASH);

	public static final String LINESTYLE_PROP = "LineStyle";

	private static final long serialVersionUID = 1;

	private boolean isConnected;

	private int lineStyle = Graphics.LINE_SOLID;

	private Connectable source;

	private Connectable target;

	public Connection(Connectable source, Connectable target) {
		reconnect(source, target);
	}

	public void disconnect() {
		if (isConnected) {
			source.removeConnection(this);
			target.removeConnection(this);
			isConnected = false;
		}
	}

	public int getLineStyle() {
		return lineStyle;
	}

	public Object getPropertyValue(Object id) {
		if (id.equals(LINESTYLE_PROP)) {
			if (getLineStyle() == Graphics.LINE_DASH)
				return new Integer(1);
			return new Integer(0);
		}
		return super.getPropertyValue(id);
	}

	public Connectable getSource() {
		return source;
	}

	public Connectable getTarget() {
		return target;
	}

	public void reconnect() {
		if (!isConnected) {
			source.addConnection(this);
			target.addConnection(this);
			isConnected = true;
		}
	}

	public void reconnect(Connectable newSource, Connectable newTarget) {
		if (newSource == null || newTarget == null || newSource == newTarget) {
			throw new IllegalArgumentException("Unable to reconnect newSource:"
					+ newSource + " newTarget:" + newTarget);
		}
		disconnect();
		this.source = newSource;
		this.target = newTarget;
		reconnect();
	}

	public void setLineStyle(int lineStyle) {
		if (lineStyle != Graphics.LINE_DASH && lineStyle != Graphics.LINE_SOLID) {
			throw new IllegalArgumentException();
		}
		this.lineStyle = lineStyle;
		firePropertyChange(LINESTYLE_PROP, null, new Integer(this.lineStyle));
	}

	public void setPropertyValue(Object id, Object value) {
		if (id.equals(LINESTYLE_PROP))
			setLineStyle(new Integer(1).equals(value) ? Graphics.LINE_DASH
					: Graphics.LINE_SOLID);
		else
			super.setPropertyValue(id, value);
	}

}