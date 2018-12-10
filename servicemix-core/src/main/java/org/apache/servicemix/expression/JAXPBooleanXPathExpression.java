/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.expression;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Evaluates an XPath expression and coerces the result into a String.
 *
 * @version $Revision: 359151 $
 */
public class JAXPBooleanXPathExpression extends JAXPXPathExpression {

    public JAXPBooleanXPathExpression() {
    }

    public JAXPBooleanXPathExpression(String xpath) throws Exception {
        super(xpath);
    }

    public Object evaluateXPath(Object object) throws XPathExpressionException {
        return getXPathExpression().evaluate(object, XPathConstants.BOOLEAN);
    }
}
