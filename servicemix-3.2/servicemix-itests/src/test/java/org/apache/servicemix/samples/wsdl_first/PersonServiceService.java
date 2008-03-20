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
package org.apache.servicemix.samples.wsdl_first;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

@WebServiceClient(targetNamespace = "http://servicemix.apache.org/samples/wsdl-first", name = "PersonService", wsdlLocation = "file:/c:/java/servicemix/clean/samples/wsdl-first/wsdl-first-jsr181-su/src/main/resources/person.wsdl")
public class PersonServiceService
    extends Service
{

    private static Map ports = new HashMap();
    public static Class Person = org.apache.servicemix.samples.wsdl_first.Person.class;

    static {
        ports.put(new QName("http://servicemix.apache.org/samples/wsdl-first", "soap"), Person);
        ports.put(new QName("http://servicemix.apache.org/samples/wsdl-first", "PersonServiceLocalPort"), Person);
    }

    public PersonServiceService()
        throws MalformedURLException
    {
        super(new URL("file:/c:/java/servicemix/clean/samples/wsdl-first/wsdl-first-jsr181-su/src/main/resources/person.wsdl"), new QName("http://servicemix.apache.org/samples/wsdl-first", "PersonService"));
    }

    public static Map getPortClassMap() {
        return ports;
    }

    @WebEndpoint(name = "soap")
    public org.apache.servicemix.samples.wsdl_first.Person getsoap() {
        return ((org.apache.servicemix.samples.wsdl_first.Person)(this).getPort(new QName("http://servicemix.apache.org/samples/wsdl-first", "soap"), Person));
    }

    @WebEndpoint(name = "PersonServiceLocalPort")
    public org.apache.servicemix.samples.wsdl_first.Person getPersonServiceLocalPort() {
        return ((org.apache.servicemix.samples.wsdl_first.Person)(this).getPort(new QName("http://servicemix.apache.org/samples/wsdl-first", "PersonServiceLocalPort"), Person));
    }

}
