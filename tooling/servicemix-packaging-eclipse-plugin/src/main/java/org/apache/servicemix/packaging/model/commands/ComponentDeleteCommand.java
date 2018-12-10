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
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.eclipse.gef.commands.Command;

/**
 * The GEF command for deleting a component
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ComponentDeleteCommand extends Command {

	private final AbstractComponent child;

	private final DeploymentDiagram parent;

	private boolean wasRemoved;

	public ComponentDeleteCommand(DeploymentDiagram parent,
			AbstractComponent component) {
		if (parent == null || component == null) {
			throw new IllegalArgumentException();
		}
		setLabel("component deletion");
		this.parent = parent;
		this.child = component;
	}

	public boolean canUndo() {
		return wasRemoved;
	}

	public void execute() {
		redo();
	}

	public void redo() {
		parent.removeChild(child);
	}

	public void undo() {
		parent.addChild(child);
	}
}
