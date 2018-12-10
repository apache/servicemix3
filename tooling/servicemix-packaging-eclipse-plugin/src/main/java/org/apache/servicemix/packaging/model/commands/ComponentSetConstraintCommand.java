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
package org.apache.servicemix.packaging.model.commands;

import org.apache.servicemix.packaging.model.AbstractComponent;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

public class ComponentSetConstraintCommand extends Command {

	private final Rectangle newBounds;

	private Rectangle oldBounds;

	private final ChangeBoundsRequest request;

	private AbstractComponent component;

	public ComponentSetConstraintCommand(AbstractComponent component,
			ChangeBoundsRequest req, Rectangle newBounds) {
		if (component == null || req == null || newBounds == null) {
			throw new IllegalArgumentException();
		}
		this.component = component;
		this.request = req;
		this.newBounds = newBounds.getCopy();
		setLabel("move");
	}

	public boolean canExecute() {
		Object type = request.getType();
		return (RequestConstants.REQ_MOVE.equals(type)
				|| RequestConstants.REQ_MOVE_CHILDREN.equals(type)
				|| RequestConstants.REQ_RESIZE.equals(type) || RequestConstants.REQ_RESIZE_CHILDREN
				.equals(type));
	}

	public void execute() {
		try {
			oldBounds = new Rectangle(component.getLocation(), component
					.getLocation());
			redo();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void redo() {
		component.setLocation(newBounds.getLocation());
	}

	public void undo() {
		component.setLocation(oldBounds.getLocation());
	}
}
