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
package org.apache.servicemix.jbi.messaging;

import java.net.URI;

/**
 * Resolver for URI patterns
 *
 * @version $Revision$
 */
public class MessageExchangeSupport {
    /**
     * In Only MEP.
     */
    public static final URI IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/in-only");
    /**
     * In Out MEP.
     */
    public static final URI IN_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-out");
    /**
     * In Optional Out MEP.
     */
    public static final URI IN_OPTIONAL_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-opt-out");
    /**
     * Robust In Only MEP.
     */
    public static final URI ROBUST_IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-in-only");
    /**
     * Out Only MEP.
     */
    public static final URI OUT_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/out-only");
    /**
     * Out In MEP.
     */
    public static final URI OUT_IN = URI.create("http://www.w3.org/2004/08/wsdl/out-in");
    /**
     * Out Optional In MEP.
     */
    public static final URI OUT_OPTIONAL_IN = URI.create("http://www.w3.org/2004/08/wsdl/out-opt-in");
    /**
     * Robust Out Only MEP.
     */
    public static final URI ROBUST_OUT_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-out-only");
}