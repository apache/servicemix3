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

import java.util.Collection;

import org.apache.servicemix.sca.assembly.sdo.JbiAssemblyPackage;
import org.apache.tuscany.model.assembly.sdo.impl.BindingImpl;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.sdo.util.BasicESequence;
import org.eclipse.emf.ecore.sdo.util.ESequence;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.osoa.sca.model.JbiBinding;

import commonj.sdo.Sequence;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Web Service Binding</b></em>'.
 * <!-- end-user-doc -->
 * <p/>
 * The following features are implemented:
 * <ul>
 * <li>{@link JbiBindingImpl#getAny <em>Any</em>}</li>
 * <li>{@link JbiBindingImpl#getPort <em>Port</em>}</li>
 * <li>{@link JbiBindingImpl#getAnyAttribute <em>Any Attribute</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class JbiBindingImpl extends BindingImpl implements JbiBinding {
    /**
     * The cached value of the '{@link #getAny() <em>Any</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAny()
     */
    protected ESequence any = null;

    /**
     * The default value of the '{@link #getPort() <em>Port</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getPort()
     */
    protected static final String PORT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPort() <em>Port</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getPort()
     */
    protected String port = PORT_EDEFAULT;

    /**
     * The cached value of the '{@link #getAnyAttribute() <em>Any Attribute</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAnyAttribute()
     */
    protected ESequence anyAttribute = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    protected JbiBindingImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    protected EClass eStaticClass() {
        return JbiAssemblyPackage.eINSTANCE.getJbiBinding();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public Sequence getAny() {
        if (any == null) {
            any = new BasicESequence(new BasicFeatureMap(this, JbiAssemblyPackage.JBI_BINDING__ANY));
        }
        return any;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public String getPort() {
        return port;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public void setPort(String newPort) {
        String oldPort = port;
        port = newPort;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JbiAssemblyPackage.JBI_BINDING__PORT, oldPort, port));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public Sequence getAnyAttribute() {
        if (anyAttribute == null) {
            anyAttribute = new BasicESequence(new BasicFeatureMap(this, JbiAssemblyPackage.JBI_BINDING__ANY_ATTRIBUTE));
        }
        return anyAttribute;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
            case JbiAssemblyPackage.JBI_BINDING__ANY:
                return ((InternalEList) ((ESequence) getAny()).featureMap()).basicRemove(otherEnd, msgs);
            case JbiAssemblyPackage.JBI_BINDING__ANY_ATTRIBUTE:
                return ((InternalEList) ((ESequence) getAnyAttribute()).featureMap()).basicRemove(otherEnd, msgs);
            default:
                return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
            }
        }
        return eBasicSetContainer(null, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
        case JbiAssemblyPackage.JBI_BINDING__URI:
            return getUri();
        case JbiAssemblyPackage.JBI_BINDING__ANY:
            return ((ESequence) getAny()).featureMap();
        case JbiAssemblyPackage.JBI_BINDING__PORT:
            return getPort();
        case JbiAssemblyPackage.JBI_BINDING__ANY_ATTRIBUTE:
            return ((ESequence) getAnyAttribute()).featureMap();
        }
        return eDynamicGet(eFeature, resolve);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public void eSet(EStructuralFeature eFeature, Object newValue) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
        case JbiAssemblyPackage.JBI_BINDING__URI:
            setUri((String) newValue);
            return;
        case JbiAssemblyPackage.JBI_BINDING__ANY:
            ((ESequence) getAny()).featureMap().clear();
            ((ESequence) getAny()).featureMap().addAll((Collection) newValue);
            return;
        case JbiAssemblyPackage.JBI_BINDING__PORT:
            setPort((String) newValue);
            return;
        case JbiAssemblyPackage.JBI_BINDING__ANY_ATTRIBUTE:
            ((ESequence) getAnyAttribute()).featureMap().clear();
            ((ESequence) getAnyAttribute()).featureMap().addAll((Collection) newValue);
            return;
        }
        eDynamicSet(eFeature, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public void eUnset(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
        case JbiAssemblyPackage.JBI_BINDING__URI:
            setUri(URI_EDEFAULT);
            return;
        case JbiAssemblyPackage.JBI_BINDING__ANY:
            ((ESequence) getAny()).featureMap().clear();
            return;
        case JbiAssemblyPackage.JBI_BINDING__PORT:
            setPort(PORT_EDEFAULT);
            return;
        case JbiAssemblyPackage.JBI_BINDING__ANY_ATTRIBUTE:
            ((ESequence) getAnyAttribute()).featureMap().clear();
            return;
        }
        eDynamicUnset(eFeature);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public boolean eIsSet(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
        case JbiAssemblyPackage.JBI_BINDING__URI:
            return URI_EDEFAULT == null ? uri != null : !URI_EDEFAULT.equals(uri);
        case JbiAssemblyPackage.JBI_BINDING__ANY:
            return any != null && !any.featureMap().isEmpty();
        case JbiAssemblyPackage.JBI_BINDING__PORT:
            return PORT_EDEFAULT == null ? port != null : !PORT_EDEFAULT.equals(port);
        case JbiAssemblyPackage.JBI_BINDING__ANY_ATTRIBUTE:
            return anyAttribute != null && !anyAttribute.featureMap().isEmpty();
        }
        return eDynamicIsSet(eFeature);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (any: ");
        result.append(any);
        result.append(", port: ");
        result.append(port);
        result.append(", anyAttribute: ");
        result.append(anyAttribute);
        result.append(')');
        return result.toString();
	}

} //WebServiceBindingImpl
