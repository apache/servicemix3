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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.servicemix.packaging.DeployerEditor;
import org.eclipse.swt.graphics.Image;

/**
 * An abstract connectionable (ie. has a Service Name) service which can be
 * either a BC or an SU
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public abstract class AbstractConnectableService extends AbstractComponent
		implements Connectable {

	public static final String SOURCE_CONNECTIONS_PROP = "Component.SourceConn";

	public static final String TARGET_CONNECTIONS_PROP = "Component.TargetConn";

	protected static Image createImage(String name) {
		InputStream stream = DeployerEditor.class.getResourceAsStream(name);
		Image image = new Image(null, stream);
		try {
			stream.close();
		} catch (IOException ioe) {
		}
		return image;
	}

	private ConnectionDecorator connectionDecorator;

	protected QName serviceName = null;

	List<Connection> sourceConnections = new ArrayList<Connection>();

	List<Connection> targetConnections = new ArrayList<Connection>();

	public AbstractConnectableService() {
		connectionDecorator = new ConnectionDecorator(this);
		addPropertyChangeListener(connectionDecorator);
	}

	public void addConnection(Connection conn) {
		if (conn == null || conn.getSource() == conn.getTarget()) {
			throw new IllegalArgumentException();
		}
		if (conn.getSource() == this) {
			sourceConnections.add(conn);
			firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
		} else if (conn.getTarget() == this) {
			targetConnections.add(conn);
			firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
		}
	}

	public void refreshConnections() {		
		firePropertyChange(SERVICENAME_PROP, null, null);
	}

	public QName getServiceName() {
		return serviceName;
	}

	public List<Connection> getSourceConnections() {
		return new ArrayList<Connection>(sourceConnections);
	}

	public List getTargetConnections() {
		return new ArrayList<Connection>(targetConnections);
	}

	public void removeConnection(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}
		if (conn.getSource() == this) {
			sourceConnections.remove(conn);
			firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
		} else if (conn.getTarget() == this) {
			targetConnections.remove(conn);
			firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
		}
	}

	public void setServiceName(QName serviceName) {
		firePropertyChange(SERVICENAME_PROP, this.serviceName, serviceName);
		this.serviceName = serviceName;
	}

}
