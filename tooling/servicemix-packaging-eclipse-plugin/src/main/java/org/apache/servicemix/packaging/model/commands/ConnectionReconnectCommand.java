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

import org.apache.servicemix.packaging.model.Connectable;
import org.apache.servicemix.packaging.model.Connection;
import org.eclipse.gef.commands.Command;

/**
 * GEF command for connection reconnection
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ConnectionReconnectCommand extends Command {

	private Connection connection;

	private Connectable newSource;

	private Connectable newTarget;

	private final Connectable oldSource;

	private final Connectable oldTarget;

	public ConnectionReconnectCommand(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}
		this.connection = conn;
		this.oldSource = conn.getSource();
		this.oldTarget = conn.getTarget();
	}

	public boolean canExecute() {
		if (newSource != null) {
			return checkSourceReconnection();
		} else if (newTarget != null) {
			return checkTargetReconnection();
		}
		return false;
	}

	private boolean checkSourceReconnection() {
		if (newSource.equals(oldTarget)) {
			return false;
		}
		for (Iterator iter = newSource.getSourceConnections().iterator(); iter
				.hasNext();) {
			Connection conn = (Connection) iter.next();
			if (conn.getTarget().equals(oldTarget) && !conn.equals(connection)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkTargetReconnection() {
		if (newTarget.equals(oldSource)) {
			return false;
		}
		for (Iterator iter = newTarget.getTargetConnections().iterator(); iter
				.hasNext();) {
			Connection conn = (Connection) iter.next();
			if (conn.getSource().equals(oldSource) && !conn.equals(connection)) {
				return false;
			}
		}
		return true;
	}

	public void execute() {
		if (newSource != null) {
			connection.reconnect(newSource, oldTarget);
		} else if (newTarget != null) {
			connection.reconnect(oldSource, newTarget);
		} else {
			throw new IllegalStateException("Should not happen");
		}
	}

	public void setNewSource(Connectable connectionSource) {
		if (connectionSource == null) {
			throw new IllegalArgumentException();
		}
		setLabel("move connection startpoint");
		newSource = connectionSource;
		newTarget = null;
	}

	public void setNewTarget(Connectable connectionTarget) {
		if (connectionTarget == null) {
			throw new IllegalArgumentException();
		}
		setLabel("move connection endpoint");
		newSource = null;
		newTarget = connectionTarget;
	}

	public void undo() {
		connection.reconnect(oldSource, oldTarget);
	}

}
