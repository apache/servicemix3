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
package org.apache.servicemix.components.validation;

import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.servicemix.jbi.FaultException;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.util.MessageUtil;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.jbi.JBIException;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;

/**
 * This component performs a schema validation on the incoming document
 * and returning a fault if the document does not conform to the schema
 * otherwise the message is passed on its way.
 *
 * @version $Revision$
 */
public class ValidateComponent extends TransformComponentSupport {

    private Schema schema;
    private String schemaLanguage = "http://www.w3.org/2001/XMLSchema";
    private Source schemaSource;
    private Resource schemaResource;
    private MessageAwareErrorHandlerFactory errorHandlerFactory = new CountingErrorHandlerFactory();
    
    public static final String FAULT_FLOW = "FAULT_FLOW";
    public static final String FAULT_JBI = "FAULT_JBI";
    
    private String handlingErrorMethod = "FAULT_JBI";

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getSchemaLanguage() {
        return schemaLanguage;
    }

    public void setSchemaLanguage(String schemaLanguage) {
        this.schemaLanguage = schemaLanguage;
    }

    public Source getSchemaSource() {
        return schemaSource;
    }

    public void setSchemaSource(Source schemaSource) {
        this.schemaSource = schemaSource;
    }

    public Resource getSchemaResource() {
        return schemaResource;
    }

    public void setSchemaResource(Resource schemaResource) {
        this.schemaResource = schemaResource;
    }
   
    public MessageAwareErrorHandlerFactory getErrorHandlerFactory() {
        return errorHandlerFactory;
    }

    public void setErrorHandlerFactory(MessageAwareErrorHandlerFactory errorHandlerFactory) {
        this.errorHandlerFactory = errorHandlerFactory;
    }

    protected void init() throws JBIException {
        super.init();

        try {
            if (schema == null) {
                SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);

                if (schemaSource == null) {
                    if (schemaResource == null) {
                        throw new JBIException("You must specify a schema, schemaSource or schemaResource property");
                    }
                    if (schemaResource.getURL() == null) {
                        schemaSource = new StreamSource(schemaResource.getInputStream());
                    } else {
                        schemaSource = new StreamSource(schemaResource.getInputStream(), schemaResource.getURL().toExternalForm()); 
                    }
                }
                schema = factory.newSchema(schemaSource);
            }
        }
        catch (IOException e) {
            throw new JBIException("Failed to load schema: " + e, e);
        }
        catch (SAXException e) {
            throw new JBIException("Failed to load schema: " + e, e);
        }
    }

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        Validator validator = schema.newValidator();

        // create a new errorHandler and set it on the validator
        MessageAwareErrorHandler errorHandler = errorHandlerFactory.createMessageAwareErrorHandler();
        validator.setErrorHandler(errorHandler);
        DOMResult result = new DOMResult();
        
        // Transform first so that the input source will be parsed only once
        // if it is a StreamSource
        getMessageTransformer().transform(exchange, in, out);
        try {
            SourceTransformer sourceTransformer = new SourceTransformer();
            // Only DOMSource and SAXSource are allowed for validating
            // See http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/Validator.html#validate(javax.xml.transform.Source,%20javax.xml.transform.Result)
            // As we expect a DOMResult as output, we must ensure that the input is a 
            // DOMSource
            DOMSource src = sourceTransformer.toDOMSource(out.getContent());
            doValidation(validator,src,result);
            if (errorHandler.hasErrors()) {
                Fault fault = exchange.createFault();
                
                // Dont set the schema and source document as properties on the fault
                // because they are not serializable
                //fault.setProperty("org.apache.servicemix.xml", src);
                // Dont set the schema because it contains an instance of
                // com.sun.org.apache.xerces.internal.jaxp.validation.xs.SchemaImpl that
                // is not serializable
                //fault.setProperty("org.apache.servicemix.schema", schema);
                
                /* 
                 * check if this error handler supports the capturing of
                 * error messages.
                 */
                if (errorHandler.capturesMessages()) {

                    /* 
                     * In descending order of preference select a format to use. If
                     * neither DOMSource, StringSource or String are supported throw
                     * a messaging exception.
                     */
                    if (errorHandler.supportsMessageFormat(DOMSource.class)) {
                        fault.setContent(
                                (DOMSource)errorHandler.getMessagesAs(DOMSource.class));
                    } else if (errorHandler.supportsMessageFormat(StringSource.class)) {
                        fault.setContent(sourceTransformer.toDOMSource(
                                (StringSource)errorHandler.getMessagesAs(StringSource.class)));
                    } else if (errorHandler.supportsMessageFormat(String.class)) {
                        fault.setContent(
                                sourceTransformer.toDOMSource(
                                        new StringSource(
                                                (String)errorHandler.getMessagesAs(String.class))));
                    } else {
                        throw new MessagingException("MessageAwareErrorHandler implementation " + 
                                errorHandler.getClass().getName() +
                                " does not support a compatible error message format.");
                    }
                } else {
                    /* 
                     * we can't do much here if the ErrorHandler implementation does
                     * not support capturing messages
                     */
                    fault.setContent(new DOMSource(result.getNode(), result.getSystemId()));
                }
                if (!handlingErrorMethod.equalsIgnoreCase(FAULT_FLOW)) {
                	// HANDLE AS JBI FAULT
                	throw new FaultException("Failed to validate against schema: " + schema, exchange, fault);
                } else {
                	MessageUtil.transfer(fault, out);
                	return true;
                }
            }
            else {
                // Retrieve the ouput of the validation
                // as it may have been changed by the validator
                out.setContent(new DOMSource(result.getNode(), result.getSystemId()));
                return true;
             }
        }
        catch (SAXException e) {
            throw new MessagingException(e);
        }
        catch (IOException e) {
            throw new MessagingException(e);
        } 
        catch (ParserConfigurationException e) {
            throw new MessagingException(e);
        } 
        catch (TransformerException e) {
            throw new MessagingException(e);
        }
    }
    
    protected void doValidation(Validator validator, DOMSource src, DOMResult result) throws SAXException, IOException {
        validator.validate(src,result);
    }

	public String getHandlingErrorMethod() {
		return handlingErrorMethod;
	}

	public void setHandlingErrorMethod(String handlingErrorMethod) {
		this.handlingErrorMethod = handlingErrorMethod;
	}

}

