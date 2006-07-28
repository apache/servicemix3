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
package org.servicemix.ws.notification;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.servicemix.wspojo.notification.PublisherRegistrationManager;
import org.servicemix.wspojo.notification.ResourceNotDestroyedFault;
import org.servicemix.wspojo.notification.ResourceUnknownFault;
import org.servicemix.wspojo.notification.TerminationTimeChangeRejectedFault;
import org.servicemix.wspojo.notification.UnableToSetTerminationTimeFault;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.JMSException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import java.rmi.RemoteException;
import java.util.Map;

public class ActiveMQPublisherRegistrationManager implements PublisherRegistrationManager {

    private Map publishers = new ConcurrentHashMap();

    public ActiveMQPublisherRegistration getProducer(EndpointReferenceType reference) {
        return (ActiveMQPublisherRegistration) publishers.get(reference);
    }

    public EndpointReferenceType register(ActiveMQPublisherRegistration publisher) {
        EndpointReferenceType answer = publisher.getPublisherReference();
        if (answer == null) {
            answer = createEndpointReference();
        }
        publisher.setEndpointReference(answer);
        publishers.put(answer, publisher);
        return answer;
    }

    protected EndpointReferenceType createEndpointReference() {
        return new EndpointReferenceType();
    }

    public void destroy(EndpointReferenceType reference) {
        ActiveMQPublisherRegistration publisher = (ActiveMQPublisherRegistration) publishers.remove(reference);
        if (publisher == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }
        try {
            publisher.stop();
        }
        catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setTerminationTime(
            EndpointReferenceType reference,
            @WebParam(name = "RequestedTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
            XMLGregorianCalendar requestedTerminationTime,
            @WebParam(name = "NewTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", mode = WebParam.Mode.OUT)
            Holder<XMLGregorianCalendar> newTerminationTime,
            @WebParam(name = "CurrentTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", mode = WebParam.Mode.OUT)
            Holder<XMLGregorianCalendar> currentTime) throws TerminationTimeChangeRejectedFault,
            UnableToSetTerminationTimeFault, ResourceUnknownFault, RemoteException {
        ActiveMQPublisherRegistration publisher = getProducer(reference);
        if (publisher == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }

        currentTime.value = publisher.getTerminationTime();
        publisher.setTerminationTime(newTerminationTime.value);
        newTerminationTime.value = publisher.getTerminationTime();
    }

    @WebMethod(operationName = "Destroy")
    @RequestWrapper(className = "org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourcelifetime_1_2_draft_01.Destroy", localName = "Destroy", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
    @ResponseWrapper(className = "org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourcelifetime_1_2_draft_01.DestroyResponse", localName = "DestroyResponse", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
    public void destroy() throws ResourceNotDestroyedFault, ResourceUnknownFault {
        /** TODO - should be removed from API */
    }

    @WebMethod(operationName = "SetTerminationTime")
    @RequestWrapper(className = "org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourcelifetime_1_2_draft_01.SetTerminationTime", localName = "SetTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
    @ResponseWrapper(className = "org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourcelifetime_1_2_draft_01.SetTerminationTimeResponse", localName = "SetTerminationTimeResponse", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
    public void setTerminationTime(
            @WebParam(name = "RequestedTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
            XMLGregorianCalendar xmlGregorianCalendar,
            @WebParam(mode = WebParam.Mode.OUT, name = "NewTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
            Holder<XMLGregorianCalendar> newTerminationTime,
            @WebParam(mode = WebParam.Mode.OUT, name = "CurrentTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
            Holder<XMLGregorianCalendar> currentTime) throws ResourceUnknownFault, TerminationTimeChangeRejectedFault,
            UnableToSetTerminationTimeFault {
        /** TODO - should be removed from API */
    }

}
