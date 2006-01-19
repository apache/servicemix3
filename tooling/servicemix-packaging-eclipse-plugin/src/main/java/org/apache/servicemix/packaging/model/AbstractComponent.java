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

import javax.xml.bind.annotation.XmlTransient;

import org.apache.servicemix.packaging.assets.StoredAssets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;

/**
 * An abstract JBI component
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class AbstractComponent extends ModelElement {

	public static final String LOCATION_PROP = "Component.Location";

	private static final long serialVersionUID = 1;

	public static final String SERVICENAME_PROP = "Component.Width";

	Point location = new Point(0, 0);

	private ModelElement parentModelElement;

	protected StoredAssets storedAssets = new StoredAssets();

	public Image getIcon() {
		return null;
	}

	public Point getLocation() {
		return location.getCopy();
	}

	@XmlTransient
	public ModelElement getParentModelElement() {
		return parentModelElement;
	}

	public StoredAssets getStoredAssets() {
		return storedAssets;
	}

	public void setLocation(Point newLocation) {
		if (newLocation == null) {
			throw new IllegalArgumentException();
		}
		location.setLocation(newLocation);
		firePropertyChange(LOCATION_PROP, null, location);
	}

	public void setParentModelElement(ModelElement parentModelElement) {
		this.parentModelElement = parentModelElement;
	}

	public void setStoredAssets(StoredAssets storedAssets) {
		this.storedAssets = storedAssets;
	}

	public void updated() {
		firePropertyChange(SERVICENAME_PROP, null, null);
	}
}
