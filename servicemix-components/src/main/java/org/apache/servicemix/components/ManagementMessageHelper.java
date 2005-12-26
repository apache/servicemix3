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
package org.apache.servicemix.components;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagementMessageHelper {
    
    private static final Log logger = LogFactory.getLog(ManagementMessageHelper.class);

    public static class Message {
        private String task;
        private String component;
        private String result;
        private Exception exception;
        private String type;
        private String message;
        
        public String getComponent() {
            return component;
        }
        public void setComponent(String component) {
            this.component = component;
        }
        public Exception getException() {
            return exception;
        }
        public void setException(Exception exception) {
            this.exception = exception;
        }
        public String getResult() {
            return result;
        }
        public void setResult(String result) {
            this.result = result;
        }
        public String getTask() {
            return task;
        }
        public void setTask(String task) {
            this.task = task;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    public static String createComponentMessage(Message msg) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
            // Start document
            xsw.writeStartDocument();
            xsw.writeCharacters("\n");
            // component-task-result
            xsw.writeStartElement("component-task-result");
            xsw.writeDefaultNamespace("http://java.sun.com/xml/ns/jbi/management-message");
            xsw.writeCharacters("\n\t");
            // component-name
            xsw.writeStartElement("component-name");
            xsw.writeCharacters(msg.getComponent());
            xsw.writeEndElement();
            // component-task-result-details
            xsw.writeCharacters("\n\t");
            xsw.writeStartElement("component-task-result-details");
            // task-result-details
            xsw.writeCharacters("\n\t\t");
            xsw.writeStartElement("task-result-details");
            // task-id
            xsw.writeCharacters("\n\t\t\t");
            xsw.writeStartElement("task-id");
            xsw.writeCharacters(msg.getTask());
            xsw.writeEndElement();
            // task-result
            xsw.writeCharacters("\n\t\t\t");
            xsw.writeStartElement("task-result");
            xsw.writeCharacters(msg.getResult());
            xsw.writeEndElement();
            // message-type
            if (msg.getType() != null) {
                xsw.writeCharacters("\n\t\t\t");
                xsw.writeStartElement("message-type");
                xsw.writeCharacters(msg.getType());
                xsw.writeEndElement();
            }
            // task-status-message
            if (msg.getMessage() != null) {
                xsw.writeCharacters("\n\t\t\t");
                xsw.writeStartElement("task-status-message");
                xsw.writeStartElement("msg-loc-info");
                xsw.writeEmptyElement("loc-token");
                xsw.writeStartElement("loc-message");
                xsw.writeCharacters(msg.getMessage());
                xsw.writeEndElement();
                xsw.writeEndElement();
                xsw.writeEndElement();
            }
            // exception-info
            if (msg.getException() != null) {
                xsw.writeCharacters("\n\t\t\t");
                xsw.writeStartElement("exception-info");
                xsw.writeCharacters("\n\t\t\t\t");
                xsw.writeStartElement("nesting-level");
                xsw.writeCharacters("1");
                xsw.writeEndElement();
                xsw.writeCharacters("\n\t\t\t\t");
                xsw.writeStartElement("msg-loc-info");
                xsw.writeCharacters("\n\t\t\t\t\t");
                xsw.writeEmptyElement("loc-token");
                xsw.writeCharacters("\n\t\t\t\t\t");
                xsw.writeStartElement("loc-message");
                xsw.writeCharacters(msg.getException().getMessage());
                xsw.writeEndElement();
                xsw.writeCharacters("\n\t\t\t\t\t");
                xsw.writeStartElement("stack-trace");
                StringWriter sw2 = new StringWriter();
                PrintWriter pw = new PrintWriter(sw2);
                msg.getException().printStackTrace(pw);
                pw.close();
                xsw.writeCData(sw2.toString());
                xsw.writeEndElement();
                xsw.writeCharacters("\n\t\t\t\t");
                xsw.writeEndElement();
                xsw.writeCharacters("\n\t\t\t");
                xsw.writeEndElement();
            }
            // end: task-result-details
            xsw.writeCharacters("\n\t\t");
            xsw.writeEndElement();
            // end: component-task-result-details
            xsw.writeCharacters("\n\t");
            xsw.writeEndElement();
            // end: component-task-result
            xsw.writeCharacters("\n");
            xsw.writeEndElement();
            // return result
            xsw.close();
            return sw.toString();
        } catch (Exception e) {
            logger.warn("Error generating component management message", e);
            return null;
        }
    }
    
}
