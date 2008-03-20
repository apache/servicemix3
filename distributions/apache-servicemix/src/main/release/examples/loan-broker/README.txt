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

Welcome to the LoanDemo Example
=============================================

This example is based on the great EIP book (http://www.enterpriseintegrationpatterns.com/ComposedMessagingExample.html).
The client and components are built and run from source using Ant, http://ant.apache.org.

To start the servicemix server using the sample configuration, you must enter the following commands:

ant setup
../../bin/servicemix servicemix.xml

This will start the loan broker component.

A simple JMS client based on lingo (http://lingo.codehaus.org/) is provided.   
Just execute 'ant' from the current directory to run the JMS client.


For more information on this example please see
    http://servicemix.apache.org/loan-broker.html
