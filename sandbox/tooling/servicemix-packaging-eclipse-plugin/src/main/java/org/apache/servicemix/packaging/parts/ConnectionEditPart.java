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
package org.apache.servicemix.packaging.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.servicemix.packaging.model.ComponentConnection;
import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;

/**
 * The GEF edit part for a connection
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ConnectionEditPart extends AbstractConnectionEditPart implements
		PropertyChangeListener {

	/**
	 * Upon activation, attach to the model element as a property change
	 * listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	protected void createEditPolicies() {		
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
				new NonResizableEditPolicy());
	}

	protected IFigure createFigure() {
		PolylineConnection connection = (PolylineConnection) super
				.createFigure();
		connection.setTargetDecoration(new PolygonDecoration());
		connection.setLineStyle(getCastedModel().getLineStyle()); 
		return connection;
	}

	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	private ComponentConnection getCastedModel() {
		return (ComponentConnection) getModel();
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getPropertyName();
		if (ComponentConnection.LINESTYLE_PROP.equals(property)) {
			((PolylineConnection) getFigure()).setLineStyle(getCastedModel()
					.getLineStyle());
		}
	}

}
