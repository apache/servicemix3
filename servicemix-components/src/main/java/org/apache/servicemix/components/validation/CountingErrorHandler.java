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
package org.apache.servicemix.components.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * A simple implementation of {@link ErrorHandler} which just counts the number of warnings, errors and fatal errors.
 *
 * @version $Revision$
 */
public class CountingErrorHandler implements ErrorHandler {
    private int warningCount;
    private int errorCount;
    private int fatalErrorCount;


    public boolean hasErrors() {
        return getErrorCount() > 0 || getFatalErrorCount() > 0;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getFatalErrorCount() {
        return fatalErrorCount;
    }

    public void warning(SAXParseException e) throws SAXException {
        ++warningCount;
    }

    public void error(SAXParseException e) throws SAXException {
        ++errorCount;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        ++fatalErrorCount;
    }
}
