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

Welcome to the ServiceMix cluster Example
=============================================

This example uses shows how to connect two ServiceMix containers in a cluster.

Before starting this example, run

  ant setup

To start the servicemix servers using the sample configurations, just run:

  cd poller
  ../../../bin/servicemix servicemix-poller.xml

and

  cd writer
  ../../../bin/servicemix servicemix-writer.xml

This will start two ServiceMix containers.

One server have a file poller component, and the other one a file writer component.
Just drop the test-file.xml in the poller/inbox directory.
It should be moved to the writer/outbox directory.

See http://servicemix.apache.org/cluster.html
for more informations.

