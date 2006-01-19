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

import java.util.Iterator;

import org.apache.servicemix.packaging.model.AbstractConnectableService;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.Connection;
import org.eclipse.gef.commands.Command;


public class ConnectionCreateCommand extends Command {
	
	private Connection connection;

	private final int lineStyle;

	private final AbstractConnectableService source;

	private BindingComponent target;

	public ConnectionCreateCommand(AbstractConnectableService source, int lineStyle) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		setLabel("connection creation");
		this.source = source;
		this.lineStyle = lineStyle;
	}

	public boolean canExecute() {
		if (source.equals(target)) {
			return false;
		}
		for (Iterator iter = source.getSourceConnections().iterator(); iter
				.hasNext();) {
			Connection conn = (Connection) iter.next();
			if (conn.getTarget().equals(target)) {
				return false;
			}
		}
		return true;
	}

	public void execute() {
		connection = new Connection(source, target);
		connection.setLineStyle(lineStyle);
	}

	public void redo() {
		connection.reconnect();
	}

	public void setTarget(BindingComponent target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.target = target;
	}

	public void undo() {
		connection.disconnect();
	}
}
