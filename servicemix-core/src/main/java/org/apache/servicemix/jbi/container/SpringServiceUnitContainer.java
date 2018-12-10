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
package org.apache.servicemix.jbi.container;

/**
 * Used to hold a Server Unit configuration.  The components
 * are registered into the JBI container using the Service Unit
 * Manager life cycle methods.
 *
 * @org.apache.xbean.XBean element="serviceunit" rootElement="true"
 *                  description="A deployable service unit container"
 * @version $Revision$
 */
public class SpringServiceUnitContainer {
    
    private ActivationSpec[] activationSpecs;

    public ActivationSpec[] getActivationSpecs() {
        return activationSpecs;
    }

    public void setActivationSpecs(ActivationSpec[] activationSpecs) {
        this.activationSpecs = activationSpecs;
    }

}
