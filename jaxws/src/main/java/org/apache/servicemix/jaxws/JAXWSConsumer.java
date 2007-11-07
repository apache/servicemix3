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
package org.apache.servicemix.jaxws;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.nmr.api.Channel;
import org.apache.servicemix.nmr.api.Endpoint;
import org.apache.servicemix.nmr.api.Exchange;
import org.w3c.dom.DocumentFragment;

public class JAXWSConsumer implements Endpoint, ServiceEndpoint {

	public void process(Exchange arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setChannel(Channel arg0) {
		// TODO Auto-generated method stub
		
	}

	public DocumentFragment getAsReference(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEndpointName() {
		// TODO Auto-generated method stub
		return null;
	}

	public QName[] getInterfaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public QName getServiceName() {
		// TODO Auto-generated method stub
		return null;
	}

}
