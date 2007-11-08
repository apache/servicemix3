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

Welcome to the ServiceMix File Binding Example
==============================================

This example shows how to use the File bindings to do a simple file 
transfer.

To download the needed dependencies, run

ant setup

To start the servicemix server using the sample configuration, just run:

../../bin/servicemix servicemix.xml

This will start a component which waits for files to arrive in the inbox 
directory.  It then does a file transfer to the outbox directory.

Just copy the test-file.xml file into the inbox directory and you should 
see it appear in the outbox directory in a few seconds.

For more information see:
    http://servicemix.apache.org/file-binding.html
