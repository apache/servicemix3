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
package org.apache.servicemix.beanflow.support;

import org.apache.servicemix.beanflow.Workflow;

/**
 * 
 * @version $Revision: $
 */
public interface Interpreter {

    /**
     * Executes the given step on the workflow
     */
    void executeStep(String nextStep, Workflow workflow);

    /**
     * Validates that all the available step values (enumeration values) are
     * available on the given workflow
     */
    void validateStepsExist(Object[] stepValues, Workflow workflow);

}
