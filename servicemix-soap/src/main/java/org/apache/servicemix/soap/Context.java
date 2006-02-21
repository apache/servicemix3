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
package org.apache.servicemix.soap;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 * @since 3.0
 */
public class Context {

	public static final String SOAP_MESSAGE = "org.apache.servicemix.SoapMessage";
	public static final String INTERFACE = "org.apache.servicemix.Interface";
	public static final String OPERATION = "org.apache.servicemix.Operation";
	public static final String SERVICE = "org.apache.servicemix.Service";
	public static final String ENDPOINT = "org.apache.servicemix.Endpoint";
	
	private Map properties;
	
	public Context() {
		this.properties = new HashMap();
	}
	
	public Object getProperty(String name) {
		return properties.get(name);
	}
	
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}
	
}
