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

This example depends on Apache ODE JBI Service Engine which has not been
released yet.  You will need to build it yourself (more informations at
http://incubator.apache.org/ode/getting-ode.html).  You will need to copy
the Service Engine installer to the install directory of this distribution
prior to the following instructions.

To compile this sample, run:
  mvn install

It provides the Loan Broker Service Assembly:
  loan-broker-sa/target/loan-broker-sa-*.zip

To install and start this sample simply copy the loan-broker-sa/target/loan-broker-sa-*.zip
into ServiceMix hotdeploy folder.

To test this sample, launch the following commands:
  ant run

To stop and uninstall this sample, remove the loan-broker-sa-*.zip from the ServiceMix
hotdeploy folder.

For more information on this example please see
  http://servicemix.apache.org/loan-broker-bpel.html
