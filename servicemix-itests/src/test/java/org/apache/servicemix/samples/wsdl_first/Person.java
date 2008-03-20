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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import org.apache.servicemix.samples.wsdl_first.types.GetPersonResponse;

@WebService(name = "Person", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface Person {


    @WebMethod(operationName = "GetPerson", action = "")
    @WebResult(name = "GetPersonResponse", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first/types")
    public GetPersonResponse getPerson(
        @WebParam(name = "GetPerson", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first/types")
        org.apache.servicemix.samples.wsdl_first.types.GetPerson GetPerson)
        throws UnknownPersonFault
    ;

}
