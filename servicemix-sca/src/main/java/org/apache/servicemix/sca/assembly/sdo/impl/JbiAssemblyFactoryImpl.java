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
package org.apache.servicemix.sca.assembly.sdo.impl;

import org.apache.servicemix.sca.assembly.sdo.JbiAssemblyFactory;
import org.apache.servicemix.sca.assembly.sdo.JbiAssemblyPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.osoa.sca.model.JbiBinding;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class JbiAssemblyFactoryImpl extends EFactoryImpl implements JbiAssemblyFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public JbiAssemblyFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
        case JbiAssemblyPackage.DOCUMENT_ROOT:
            return (EObject) createDocumentRoot();
        case JbiAssemblyPackage.JBI_BINDING:
            return (EObject) createJbiBinding();
        default:
            throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public org.apache.servicemix.sca.assembly.sdo.DocumentRoot createDocumentRoot() {
        DocumentRootImpl documentRoot = new DocumentRootImpl();
        return documentRoot;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public org.osoa.sca.model.JbiBinding createJbiBindingGen() {
        JbiBindingImpl jbiBinding = new JbiBindingImpl();
        return jbiBinding;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public JbiAssemblyPackage getJbiAssemblyPackage() {
        return (JbiAssemblyPackage) getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @deprecated
     */
    public static JbiAssemblyPackage getPackage() {
        return JbiAssemblyPackage.eINSTANCE;
    }

    /**
     * Custom code
     */

    private final org.apache.servicemix.sca.assembly.JbiAssemblyFactory logicalModelFactory = new org.apache.servicemix.sca.assembly.impl.JbiAssemblyFactoryImpl();

    /**
     * @see org.apache.tuscany.binding.axis.assembly.sdo.WebServiceAssemblyFactory#createWebServiceBinding()
     */
    public JbiBinding createJbiBinding() {
        return (JbiBinding) logicalModelFactory.createJbiBinding();
    }

} //WebServiceAssemblyFactoryImpl
