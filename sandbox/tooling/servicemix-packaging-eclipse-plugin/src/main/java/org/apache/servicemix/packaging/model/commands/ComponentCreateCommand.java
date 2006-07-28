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
package org.apache.servicemix.packaging.model.commands;

import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

/**
 * The GEF command for creating a component
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ComponentCreateCommand extends Command {

	private DeploymentDiagram parent;

	private Rectangle bounds;

	private AbstractComponent component;

	public ComponentCreateCommand(AbstractComponent component,
			DeploymentDiagram parent, Rectangle bounds) {
		this.component = component;
		this.parent = parent;
		this.bounds = bounds;
		setLabel("create");
	}

	public boolean canExecute() {
		return component != null && parent != null && bounds != null;
	}

	public void execute() {
		component.setLocation(bounds.getLocation());
		redo();
	}

	public void redo() {
		parent.addChild(component);
	}

	public void undo() {
		parent.removeChild(component);
	}
}
