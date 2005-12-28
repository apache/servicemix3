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
import org.apache.tuscany.model.assembly.sdo.AssemblyPackage;
import org.apache.tuscany.model.assembly.sdo.impl.AssemblyPackageImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;
import org.osoa.sca.model.JbiBinding;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class JbiAssemblyPackageImpl extends EPackageImpl implements JbiAssemblyPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass documentRootEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass jbiBindingEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.apache.tuscany.binding.axis.assembly.sdo.WebServiceAssemblyPackage#eNS_URI
     * @see #init()
     */
    private JbiAssemblyPackageImpl() {
        super(eNS_URI, JbiAssemblyFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this
     * model, and for any others upon which it depends.  Simple
     * dependencies are satisfied by calling this method on all
     * dependent packages before doing anything else.  This method drives
     * initialization for interdependent packages directly, in parallel
     * with this package, itself.
     * <p>Of this package and its interdependencies, all packages which
     * have not yet been registered by their URI values are first created
     * and registered.  The packages are then initialized in two steps:
     * meta-model objects for all of the packages are created before any
     * are initialized, since one package's meta-model objects may refer to
     * those of another.
     * <p>Invocation of this method will not affect any packages that have
     * already been initialized.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     */
    public static JbiAssemblyPackage init() {
        if (isInited) return (JbiAssemblyPackage) EPackage.Registry.INSTANCE.getEPackage(JbiAssemblyPackage.eNS_URI);

        // Obtain or create and register package
        JbiAssemblyPackageImpl theWebServiceAssemblyPackage = (JbiAssemblyPackageImpl) (EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof JbiAssemblyPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new JbiAssemblyPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        AssemblyPackageImpl.init();
        XMLTypePackageImpl.init();

        // Create package meta-data objects
        theWebServiceAssemblyPackage.createPackageContents();

        // Initialize created meta-data
        theWebServiceAssemblyPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theWebServiceAssemblyPackage.freeze();

        return theWebServiceAssemblyPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EClass getDocumentRoot() {
        return documentRootEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EAttribute getDocumentRoot_Mixed() {
        return (EAttribute) documentRootEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EReference getDocumentRoot_XMLNSPrefixMap() {
        return (EReference) documentRootEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EReference getDocumentRoot_XSISchemaLocation() {
        return (EReference) documentRootEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EReference getDocumentRoot_BindingJbi() {
        return (EReference) documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EClass getJbiBinding() {
        return jbiBindingEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EAttribute getJbiBinding_Any() {
        return (EAttribute) jbiBindingEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EAttribute getJbiBinding_Port() {
        return (EAttribute) jbiBindingEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public EAttribute getJbiBinding_AnyAttribute() {
        return (EAttribute) jbiBindingEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public JbiAssemblyFactory getJbiAssemblyFactory() {
        return (JbiAssemblyFactory) getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__BINDING_JBI);

        jbiBindingEClass = createEClass(JBI_BINDING);
        createEAttribute(jbiBindingEClass, JBI_BINDING__ANY);
        createEAttribute(jbiBindingEClass, JBI_BINDING__PORT);
        createEAttribute(jbiBindingEClass, JBI_BINDING__ANY_ATTRIBUTE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public void initializePackageContentsGen() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        AssemblyPackageImpl theAssemblyPackage = (AssemblyPackageImpl) EPackage.Registry.INSTANCE.getEPackage(AssemblyPackage.eNS_URI);
        XMLTypePackageImpl theXMLTypePackage = (XMLTypePackageImpl) EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

        // Add supertypes to classes
        jbiBindingEClass.getESuperTypes().add(theAssemblyPackage.getBinding());

        // Initialize classes and features; add operations and parameters
        initEClass(documentRootEClass, org.apache.servicemix.sca.assembly.sdo.DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_BindingJbi(), this.getJbiBinding(), null, "bindingWs", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(jbiBindingEClass, JbiBinding.class, "JbiBinding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getJbiBinding_Any(), ecorePackage.getEFeatureMapEntry(), "any", null, 0, -1, JbiBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getJbiBinding_Port(), theXMLTypePackage.getAnyURI(), "port", null, 1, 1, JbiBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getJbiBinding_AnyAttribute(), ecorePackage.getEFeatureMapEntry(), "anyAttribute", null, 0, -1, JbiBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        createResource(eNS_URI);

        // Create annotations
        // http:///org/eclipse/emf/ecore/util/ExtendedMetaData
        createExtendedMetaDataAnnotations();
    }

    /**
     * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    protected void createExtendedMetaDataAnnotations() {
        String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
        addAnnotation
                (documentRootEClass,
                        source,
                        new String[]{
                                "name", "",
                                "kind", "mixed"
                        });
        addAnnotation
                (getDocumentRoot_Mixed(),
                        source,
                        new String[]{
                                "kind", "elementWildcard",
                                "name", ":mixed"
                        });
        addAnnotation
                (getDocumentRoot_XMLNSPrefixMap(),
                        source,
                        new String[]{
                                "kind", "attribute",
                                "name", "xmlns:prefix"
                        });
        addAnnotation
                (getDocumentRoot_XSISchemaLocation(),
                        source,
                        new String[]{
                                "kind", "attribute",
                                "name", "xsi:schemaLocation"
                        });
        addAnnotation
                (getDocumentRoot_BindingJbi(),
                        source,
                        new String[]{
                                "kind", "element",
                                "name", "binding.jbi",
                                "namespace", "##targetNamespace",
                                "affiliation", "http://www.osoa.org/xmlns/sca/0.9#binding"
                        });
        addAnnotation
                (jbiBindingEClass,
                        source,
                        new String[]{
                                "name", "ServiceMixBinding",
                                "kind", "elementOnly"
                        });
        addAnnotation
                (getJbiBinding_Any(),
                        source,
                        new String[]{
                                "kind", "elementWildcard",
                                "wildcards", "##other",
                                "name", ":1",
                                "processing", "lax"
                        });
        addAnnotation
                (getJbiBinding_Port(),
                        source,
                        new String[]{
                                "kind", "attribute",
                                "name", "port"
                        });
        addAnnotation
                (getJbiBinding_AnyAttribute(),
                        source,
                        new String[]{
                                "kind", "attributeWildcard",
                                "wildcards", "##any",
                                "name", ":3",
                                "processing", "lax"
                        });
    }

    /**
     * Custom code
     */

    /**
     * Initialize the package
     */
    public void initializePackageContents() {
        initializePackageContentsGen();
        AssemblyPackage.eINSTANCE.merge(this);
	}

} //WebServiceAssemblyPackageImpl
