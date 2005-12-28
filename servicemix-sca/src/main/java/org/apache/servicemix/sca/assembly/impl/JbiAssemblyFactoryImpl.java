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
package org.apache.servicemix.sca.assembly.impl;

import org.apache.servicemix.sca.assembly.JbiAssemblyFactory;
import org.apache.tuscany.model.assembly.impl.AssemblyFactoryImpl;
import org.osoa.sca.model.JbiBinding;

/**
 * An implementation of the model <b>Factory</b>.
 */
public class JbiAssemblyFactoryImpl extends AssemblyFactoryImpl implements JbiAssemblyFactory {

    /**
     * Creates an instance of the factory.
     */
    public JbiAssemblyFactoryImpl() {
        super();
    }

    /**
     * @see org.apache.servicemix.sca.assembly#createJbiBinding()
     */
    public JbiBinding createJbiBinding() {
        return new JbiBindingImpl();
    }

} //WebServiceAssemblyFactoryImpl
