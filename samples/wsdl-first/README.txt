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

Welcome to ServiceMix WSDL design first example
===============================================

This example shows how to expose a service, beginning from the
WSDL, and exposing it over HTTP.

First start a ServiceMix server (if not already started) by running
  bin/servicemix
in the root dir of this distribution.

To compile this sample, run:
  mvn install

It provides the WSDL First Service Assembly:
  wsdl-first-sa/target/wsdl-first-sa-*.zip

To install and start this sample simply copy the wsdl-first-sa/target/wsdl-first-sa-*.zip
into ServiceMix hotdeploy folder.

You can browse the WSDL at
  http://localhost:8192/PersonService/main.wsdl

To stop and uninstall this sample, remove the wsdl-first-sa-*.zip from the ServiceMix
hotdeploy folder.

You can also open the client.html page in a browser
to send a request to the service.

