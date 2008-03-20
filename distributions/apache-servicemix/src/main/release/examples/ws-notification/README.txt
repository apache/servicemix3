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

Welcome to the ServiceMix Web Services - Notification Example
=============================================================

This example demonstrates the ability to use the Web Services - 
Notification implementation in ServiceMix. 
First, in this directory, run 'ant' to add needed dependencies to
the classpath.  
To run this example, open three terminals - one for each instance 
directory. In each terminal, start ServiceMix and feed the 
configuration like so: 

In terminal one: 
$ cd ./instance1 
$ ../../../bin/servicemix ./servicemix1.xml

In terminal two: 
$ cd ./instance2
$ ../../../bin/servicemix ./servicemix2.xml

In terminal three: 
$ cd ./instance3
$ ../../../bin/servicemix ./servicemix3.xml

ServiceMix instance3 will publish messages to the topic named MyTopic and 
ServiceMix instance2 will receive these messages because it is subscribed to 
the topic named MyTopic.

WS-Notification demand publishing and filters will be added soon. 

See http://servicemix.apache.org/ws-notification-clustered.html
for more informations.
