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
package org.apache.servicemix.components.script;

import org.apache.servicemix.expression.Expression;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessagingException;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

/**
 * An {@link Expression} implementation using
 * <a href="http://servicemix.org/JSR+223">JSR 223</a> to allow any scripting language to be used as an expression
 * language.
 *
 * @version $Revision$
 */
public class ScriptExpression implements Expression {
    private CompiledScript compiledScript;

    public Object evaluate(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
        try {
             /** TODO */
            ScriptContext namespace = null;
            return compiledScript.eval(namespace);
        }
        catch (ScriptException e) {
            throw new MessagingException(e);
        }
    }
}
