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

Welcome to the ServiceMix Bridge Example
========================================

This example shows a bridge between the HTTP and JMS protocols,
with an XSLT transformation in between.

First start a ServiceMix server (if not already started) by running
  bin/servicemix
in the root dir of this ditribution.

To start this sample, run:
  mvn install
  cd bridge-sa
  mvn jbi:projectDeploy

For more information on running this example please see:
  http://incubator.apache.org/servicemix/sm30ug/bridge.html

