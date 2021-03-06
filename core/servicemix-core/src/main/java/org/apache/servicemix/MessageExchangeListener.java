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
package org.apache.servicemix;

/**
 * If a Component implements this interface, MessageExchange will be delivered directly to the listener
 * asynchronously rather than using the usual asynchronous delivery with a thread used up per consuming
 * component.
 * 
 * @version $Revision$
 * @deprecated
 */
public interface MessageExchangeListener extends org.apache.servicemix.jbi.listener.MessageExchangeListener {

}
