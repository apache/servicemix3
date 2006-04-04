/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.ws.rm;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2005._02.rm.CreateSequenceResponseType;
import org.xmlsoap.schemas.ws._2005._02.rm.CreateSequenceType;
import org.xmlsoap.schemas.ws._2005._02.rm.Expires;
import org.xmlsoap.schemas.ws._2005._02.rm.Identifier;
import org.xmlsoap.schemas.ws._2005._02.rm.TerminateSequenceType;
import org.xmlsoap.schemas.ws._2005._02.rm.wsdl.SequenceAbstractPortType;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision$
 */
public class SequenceManagerTest extends TestCase {
    protected SequenceAbstractPortType sequenceManager;
    protected boolean specifyExpires = false;

    public void testCreateAndTerminateSequence() throws Exception {
        CreateSequenceType createArguments = new CreateSequenceType();

        EndpointReferenceType reference = new EndpointReferenceType();
        reference.setAddress(new AttributedURI());
        reference.getAddress().setValue("http://localhost/test/" + getClass().getName() + "/" + getName());
        createArguments.setAcksTo(reference);

        if (specifyExpires) {
            Expires expires = new Expires();
            createArguments.setExpires(expires);
        }

        CreateSequenceResponseType response = sequenceManager.createSequence(createArguments);
        Identifier identifier = response.getIdentifier();
        String value = identifier.getValue();
        assertNotNull("Should have an identifier", value);
        
        System.out.println("Created identifier: " + value);
        System.out.println("Accept: " + response.getAccept());
        
        TerminateSequenceType terminateArgs = new TerminateSequenceType();
        terminateArgs.setIdentifier(identifier);
        sequenceManager.terminateSequence(terminateArgs);
    }

    protected void setUp() throws Exception {
        super.setUp();
        sequenceManager = createSequenceManager();
    }

    protected SequenceAbstractPortType createSequenceManager() {
        return new SequenceManager(createSequenceStore());
    }

    protected SequenceStore createSequenceStore() {
        return new NonPersistentSequenceStore();
    }

}
