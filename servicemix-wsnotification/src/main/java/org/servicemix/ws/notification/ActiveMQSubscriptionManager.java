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

package org.servicemix.ws.notification;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetResourcePropertyResponse;
import org.servicemix.wspojo.notification.InvalidResourcePropertyQNameFault;
import org.servicemix.wspojo.notification.PauseFailedFault;
import org.servicemix.wspojo.notification.ResourceNotDestroyedFault;
import org.servicemix.wspojo.notification.ResourceUnknownFault;
import org.servicemix.wspojo.notification.ResumeFailedFault;
import org.servicemix.wspojo.notification.SubscriptionManager;
import org.servicemix.wspojo.notification.TerminationTimeChangeRejectedFault;
import org.servicemix.wspojo.notification.UnableToSetTerminationTimeFault;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.JMSException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import java.rmi.RemoteException;

public class ActiveMQSubscriptionManager implements SubscriptionManager {

    private final ConcurrentHashMap subscriptions = new ConcurrentHashMap();

    public EndpointReferenceType register(ActiveMQSubscription subscription) {
        EndpointReferenceType reference = subscription.getConsumerReference();
        subscriptions.put(reference, subscription);
        return reference;
    }

    public ActiveMQSubscription getSubscription(EndpointReferenceType subscriptionReference) {
        return (ActiveMQSubscription) subscriptions.get(subscriptionReference);
    }

    public ActiveMQSubscription removeSubscription(EndpointReferenceType subscriptionReference) {
        return (ActiveMQSubscription) subscriptions.remove(subscriptionReference);
    }

    // TODO add to SubscriptionManager ASAP
    // -------------------------------------------------------------------------
    public void destroy(EndpointReferenceType resource) {
        ActiveMQSubscription subscription = removeSubscription(resource);
        if (subscription == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }
        try {
            subscription.close();
        }
        catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void pauseSubcription(EndpointReferenceType resource) {
        ActiveMQSubscription subscription = getSubscription(resource);
        if (subscription == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }
        try {
            subscription.stop();
        }
        catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void resumeSubscription(EndpointReferenceType resource) {
        ActiveMQSubscription subscription = getSubscription(resource);
        if (subscription == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }
        try {
            subscription.start();
        }
        catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setTerminationTime(
            EndpointReferenceType resource,
            @WebParam(name = "RequestedTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
            XMLGregorianCalendar requestedTerminationTime,
            @WebParam(name = "NewTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", mode = WebParam.Mode.OUT)
            Holder<XMLGregorianCalendar> newTerminationTime,
            @WebParam(name = "CurrentTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", mode = WebParam.Mode.OUT)
            Holder<XMLGregorianCalendar> currentTime) throws TerminationTimeChangeRejectedFault,
            UnableToSetTerminationTimeFault, ResourceUnknownFault, RemoteException {
        ActiveMQSubscription subscription = getSubscription(resource);
        if (subscription == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }

        currentTime.value = subscription.getTerminationTime();
        subscription.setTerminationTime(newTerminationTime.value);
        newTerminationTime.value = subscription.getTerminationTime();
    }

    @WebMethod(operationName = "GetResourceProperty", action = "http://servicemix.org/wspojo/notification/GetResourceProperty")
    @WebResult(name = "GetResourcePropertyResponse", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public GetResourcePropertyResponse getResourceProperty(
            EndpointReferenceType resource,
            @WebParam(name = "GetResourceProperty", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
            QName getResourcePropertyRequest) throws InvalidResourcePropertyQNameFault, ResourceUnknownFault,
            RemoteException {
        ActiveMQSubscription subscription = removeSubscription(resource);
        if (subscription == null) {
            throw new RuntimeException("Invalid endpoint reference.");
        }
        return subscription.getResourceProperty(resource, getResourcePropertyRequest);
    }

    // SubscriptionManager interface
    // -------------------------------------------------------------------------

    @WebMethod(operationName = "GetResourceProperty", action = "http://servicemix.org/wspojo/notification/GetResourceProperty")
    @WebResult(name = "GetResourcePropertyResponse", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public GetResourcePropertyResponse getResourceProperty(
            @WebParam(name = "GetResourceProperty", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
            QName getResourcePropertyRequest) throws InvalidResourcePropertyQNameFault, ResourceUnknownFault {
        /** TODO - should be removed from API */
        return null;
    }

    @WebMethod(operationName = "Destroy", action = "http://servicemix.org/wspojo/notification/Destroy")
    public void destroy() throws ResourceNotDestroyedFault, ResourceUnknownFault {
        /** TODO - should be removed from API */
    }

    @WebMethod(operationName = "SetTerminationTime", action = "http://servicemix.org/wspojo/notification/SetTerminationTime")
    public void setTerminationTime(
            @WebParam(name = "RequestedTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd")
            XMLGregorianCalendar requestedTerminationTime,
            @WebParam(name = "NewTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", mode = WebParam.Mode.OUT)
            Holder<XMLGregorianCalendar> newTerminationTime,
            @WebParam(name = "CurrentTime", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", mode = WebParam.Mode.OUT)
            Holder<XMLGregorianCalendar> currentTime) throws TerminationTimeChangeRejectedFault,
            UnableToSetTerminationTimeFault, ResourceUnknownFault {
        /** TODO - should be removed from API */
    }

    @WebMethod(operationName = "PauseSubscription", action = "http://servicemix.org/wspojo/notification/PauseSubscription")
    @WebResult(name = "PauseSubscriptionResponse", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public Object pauseSubscription(
            @WebParam(name = "PauseSubscription", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            Object request) throws PauseFailedFault, ResourceUnknownFault {
        /** TODO - should be removed from API */
        if (request instanceof EndpointReferenceType) {
            pauseSubcription((EndpointReferenceType) request);
        }
        return null;
    }

    @WebMethod(operationName = "ResumeSubscription", action = "http://servicemix.org/wspojo/notification/ResumeSubscription")
    @WebResult(name = "ResumeSubscriptionResponse", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public Object resumeSubscription(
            @WebParam(name = "ResumeSubscription", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            Object request) throws ResourceUnknownFault, ResumeFailedFault {
        /** TODO - should be removed from API */
        if (request instanceof EndpointReferenceType) {
            resumeSubscription((EndpointReferenceType) request);
        }
        return null;
    }
}
