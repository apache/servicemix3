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
package org.apache.servicemix.soap.util.stax;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

/**
 * Test case for {@link StaxSource}
 */
public class StaxSourceTest extends TestCase {
    
    private static final String XML = "<test id='001'>contents</test>";
    private StaxSource source;
    private Mockery context;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new StaxSource(StaxUtil.createReader(new StringSource(XML)));
        context = new Mockery();
    }
    
    public void testNoNullUris() throws Exception {
        final ContentHandler handler = context.mock(ContentHandler.class);
        source.setContentHandler(handler);
        context.checking(new Expectations() {{
           one(handler).startDocument();
           one(handler).startElement(with(""), with("test"), with("test"), with(noNullUris()));
           allowing(anything());
        }});
        source.parse();
    }
    
    @Factory
    private static Matcher<Attributes> noNullUris() {
        return new TypeSafeMatcher<Attributes>() {

            @Override
            public boolean matchesSafely(Attributes attributes) {
                //not sure why I need the fail here, returning false should be sufficient
                if (attributes.getURI(0) == null) {
                    fail("URI should not be null, but an empty string");
                }
                return attributes.getURI(0) != null;
            }
            
            public void describeTo(Description description) {
                description.appendText("Attributes URI matches empty String");
            }
        };
    }
}
