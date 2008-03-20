/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.rm;

import org.xmlsoap.schemas.ws._2005._02.rm.AckRequestedType;
import org.xmlsoap.schemas.ws._2005._02.rm.CreateSequenceResponseType;
import org.xmlsoap.schemas.ws._2005._02.rm.CreateSequenceType;
import org.xmlsoap.schemas.ws._2005._02.rm.Expires;
import org.xmlsoap.schemas.ws._2005._02.rm.Identifier;
import org.xmlsoap.schemas.ws._2005._02.rm.SequenceAcknowledgement;
import org.xmlsoap.schemas.ws._2005._02.rm.SequenceType;
import org.xmlsoap.schemas.ws._2005._02.rm.TerminateSequenceType;
import org.xmlsoap.schemas.ws._2005._02.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.xmlsoap.schemas.ws._2005._02.rm.wsdl.SequenceAbstractPortType;

import java.math.BigInteger;

/**
 * Default implementation of the WS-RM endpoint for managing sequences.
 * 
 * @version $Revision$
 */
public class SequenceManager implements SequenceAbstractPortType {

    private static final BigInteger MAX_INTEGER = new BigInteger("" + Integer.MAX_VALUE);
    
    private SequenceStore sequenceStore;

    

    public SequenceManager(SequenceStore store) {
        sequenceStore = store;
    }

    public SequenceAcknowledgement assertValid(SequenceType sequence) {
        Identifier identifier = sequence.getIdentifier();
        Sequence s = sequenceStore.retrieve(identifier);

        if (s == null) {
            throw new SoapFault("The value of wsrm:Identifier is not a known Sequence identifier", "Sender",
                    "wsrm:UnknownSequence", identifier.toString());
        }

        // Is the message number out of range?
        BigInteger value = sequence.getMessageNumber();
        if (value.compareTo(BigInteger.ZERO) <= 0 || value.compareTo(MAX_INTEGER) > 0) {

            // We must terminate the sequence now.
            sequenceStore.delete(identifier);

            throw new SoapFault("The maximum value for wsrm:MessageNumber has been exceeded", "Sender",
                    "wsrm:MessageNumberRollover", identifier.toString());
        }

        int intValue = value.intValue();

        // If we received the last message, then check to see if the message
        // being
        // processed exceeds it's sequence.
        if (s.lastMessageNumber > 0 && intValue > s.lastMessageNumber) {
            throw new SoapFault(
                    "The value for wsrm:MessageNumber exceeds the value of the MessageNumber  accompanying a LastMessage element in this Sequence.",
                    "Sender", "wsrm:LastMessageNumberExceeded", identifier.toString());
        }

        // Is this message comming out of order??
        if (intValue != s.lastMessageAcked + 1) {
            // This implementation is really simple and just drops out of order
            // messages.

            SequenceAcknowledgement acknowledgement = new SequenceAcknowledgement();
            acknowledgement.setIdentifier(identifier);
            if (s.lastMessageAcked > 0) {
                AcknowledgementRange range = new AcknowledgementRange();
                range.setLower(BigInteger.ONE);
                range.setUpper(new BigInteger("" + s.lastMessageAcked));
                acknowledgement.getAcknowledgementRange().add(range);
            }
            return acknowledgement;
        }

        return null;
    }

    public SequenceAcknowledgement acknowledge(SequenceType sequence) {
        Identifier identifier = sequence.getIdentifier();

        // We might need something like a retrieve for update so that
        // we can lock this record across a cluster
        Sequence s = sequenceStore.retrieve(identifier);

        int value = sequence.getMessageNumber().intValue();
        s.lastMessageAcked = value;
        if (sequence.getLastMessage() != null) {
            s.lastMessageNumber = value;
        }

        sequenceStore.update(s);

        SequenceAcknowledgement acknowledgement = new SequenceAcknowledgement();
        acknowledgement.setIdentifier(sequence.getIdentifier());
        AcknowledgementRange range = new AcknowledgementRange();
        range.setLower(BigInteger.ONE);
        range.setUpper(new BigInteger("" + s.lastMessageAcked));
        acknowledgement.getAcknowledgementRange().add(range);
        return acknowledgement;
    }

    public SequenceAcknowledgement acknowledgeRequested(AckRequestedType sequence) {
        Identifier identifier = sequence.getIdentifier();
        Sequence s = sequenceStore.retrieve(identifier);

        if (s == null) {
            throw new SoapFault("The value of wsrm:Identifier is not a known Sequence identifier", "Sender",
                    "wsrm:UnknownSequence", sequence.getIdentifier().toString());
        }

        SequenceAcknowledgement acknowledgement = new SequenceAcknowledgement();
        acknowledgement.setIdentifier(sequence.getIdentifier());
        if (s.lastMessageAcked > 0) {
            AcknowledgementRange range = new AcknowledgementRange();
            range.setLower(BigInteger.ONE);
            range.setUpper(new BigInteger("" + s.lastMessageAcked));
            acknowledgement.getAcknowledgementRange().add(range);
        }
        return acknowledgement;
    }

    // SequenceAbsractPortType interface
    // -------------------------------------------------------------------------
    public CreateSequenceResponseType createSequence(CreateSequenceType createSequence) {
        Sequence s = new Sequence();
        s.acksTo = createSequence.getAcksTo();
        if (createSequence.getExpires() != null) {
            s.expires = convertToLocalTime(createSequence.getExpires());
        }
        sequenceStore.create(s);

        CreateSequenceResponseType response = new CreateSequenceResponseType();
        response.setIdentifier(s.getIdentifier());
        if (s.expires != 0) {
            response.setExpires(convertToDuration(s.expires));
        }
        return response;
    }

    public void terminateSequence(TerminateSequenceType terminateSequence) {
        sequenceStore.delete(terminateSequence.getIdentifier());
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    /**
     * TODO: Need to implement these to support expiring subscriptions
     * 
     * @param expires
     * @return
     */
    private Expires convertToDuration(long expires) {
        return null;
    }

    /**
     * TODO: Need to implement these to support expiring subscriptions
     * 
     * @param expires
     * @return
     */
    private long convertToLocalTime(Expires expires) {
        return 0;
    }

}
