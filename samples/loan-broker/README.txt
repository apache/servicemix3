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

Welcome to the Servicemix Loan Broker example
=============================================

This example is based on the great EIP book 
(http://www.enterpriseintegrationpatterns.com/ComposedMessagingExample.html).
It leverages the BPEL service engine, JMS binding component and some 
lightweight components provided by ServiceMix.

First start a ServiceMix server (if not already started) by running
  bin/servicemix
in the root dir of this ditribution.

To run this sample, launch the following commands:
  mvn install
  cd loan-broker-sa
  mvn jbi:projectDeploy

For more information on this example please see
  http://incubator.apache.org/servicemix/sm30ug/loan-broker-bpel.html
