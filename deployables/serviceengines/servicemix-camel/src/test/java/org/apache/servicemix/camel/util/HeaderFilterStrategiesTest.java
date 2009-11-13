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
package org.apache.servicemix.camel.util;

import junit.framework.TestCase;
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * Test cases for {@link org.apache.servicemix.camel.util.HeaderFilterStrategies}
 */
public class HeaderFilterStrategiesTest extends TestCase {

    public void testStrategies() {
        HeaderFilterStrategies strategies = new HeaderFilterStrategies();
        strategies.add(new HeaderFilterStrategy() {

            public boolean applyFilterToCamelHeaders(String s, Object o) {
                return "A".equals(s);
            }

            public boolean applyFilterToExternalHeaders(String s, Object o) {
                return "1".equals(s);
            }
        });
        strategies.add(new HeaderFilterStrategy() {

            public boolean applyFilterToCamelHeaders(String s, Object o) {
                return "B".equals(s);
            }

            public boolean applyFilterToExternalHeaders(String s, Object o) {
                return "2".equals(s);
            }
        });

        assertTrue("A should have been filtered", strategies.applyFilterToCamelHeaders("A", null));
        assertTrue("B should have been filtered", strategies.applyFilterToCamelHeaders("B", null));
        assertFalse("C should not have been filtered", strategies.applyFilterToCamelHeaders("C", null));

        assertTrue("1 should have been filtered", strategies.applyFilterToExternalHeaders("1", null));
        assertTrue("2 should have been filtered", strategies.applyFilterToExternalHeaders("2", null));
        assertFalse("3 should not have been filtered", strategies.applyFilterToExternalHeaders("3", null));
    }

    public void testIgnoreNullStrategies() {
        HeaderFilterStrategies strategies = new HeaderFilterStrategies();
        strategies.add(null);

        assertFalse("XYZ should not have been filtered", strategies.applyFilterToCamelHeaders("XYZ", null));
    }
}
