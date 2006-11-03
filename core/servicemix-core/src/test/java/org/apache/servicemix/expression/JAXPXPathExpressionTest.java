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
package org.apache.servicemix.expression;

import org.apache.servicemix.expression.JAXPStringXPathExpression;


/**
 * @version $Revision$
 */
public class JAXPXPathExpressionTest extends XPathExpressionTest {

    /**
     * Note that this test only works on Java 5
     *
     * @throws Exception
     */
    public void testXPathUsingJAXP() throws Exception {
        boolean test = false;

        try {
            Class.forName("java.lang.annotation.AnnotationTypeMismatchException");

            test = true;
        } catch (ClassNotFoundException doNothing) {
        }

        if (test) {
            assertExpression(new JAXPStringXPathExpression("/foo/bar/@xyz"), "cheese", "<foo><bar xyz='cheese'/></foo>");
            assertExpression(new JAXPStringXPathExpression("$name"), "James", "<foo><bar xyz='cheese'/></foo>");
        }
    }
}
