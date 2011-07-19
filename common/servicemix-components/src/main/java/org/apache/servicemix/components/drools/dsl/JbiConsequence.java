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
package org.apache.servicemix.components.drools.dsl;

import org.apache.servicemix.components.drools.JbiHelper;
import org.drools.spi.Consequence;
import org.drools.spi.ConsequenceException;
import org.drools.spi.Tuple;

import javax.jbi.messaging.MessagingException;

/**
 * @version $Revision$
 */
public abstract class JbiConsequence implements Consequence {

    public void invoke(Tuple tuple) throws ConsequenceException {
        JbiHelper helper = (JbiHelper) tuple.getWorkingMemory().getApplicationData("jbi");
        if (helper == null) {
            throw new ConsequenceException("No 'jbi' application data available");
        }
        try {
            invokeJbiOperation(helper, tuple);
        }
        catch (MessagingException e) {
            throw new ConsequenceException(e);
        }
    }
    
    protected abstract void invokeJbiOperation(JbiHelper helper, Tuple tuple) throws MessagingException;

}
