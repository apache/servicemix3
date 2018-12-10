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
package org.apache.servicemix.sca.builder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.servicemix.sca.assembly.JbiBinding;
import org.apache.servicemix.sca.config.JbiServiceEntryPointContextFactory;
import org.apache.tuscany.core.builder.BuilderException;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.core.builder.impl.EntryPointContextFactory;
import org.apache.tuscany.core.config.JavaIntrospectionHelper;
import org.apache.tuscany.core.context.AggregateContext;
import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.invocation.Interceptor;
import org.apache.tuscany.core.invocation.InvocationConfiguration;
import org.apache.tuscany.core.invocation.InvocationRuntimeException;
import org.apache.tuscany.core.invocation.ProxyConfiguration;
import org.apache.tuscany.core.invocation.TargetInvoker;
import org.apache.tuscany.core.invocation.spi.ProxyFactory;
import org.apache.tuscany.core.invocation.spi.ProxyFactoryFactory;
import org.apache.tuscany.core.message.Message;
import org.apache.tuscany.core.message.MessageFactory;
import org.apache.tuscany.core.runtime.RuntimeContext;
import org.apache.tuscany.core.system.annotation.Autowire;
import org.apache.tuscany.model.assembly.AssemblyModelObject;
import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.Service;
import org.apache.tuscany.model.assembly.ServiceContract;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Scope;

@Scope("MODULE")
public class JbiServiceEntryPointBuilder implements ContextFactoryBuilder<AggregateContext> {

    private RuntimeContext runtimeContext;

    private ProxyFactoryFactory proxyFactoryFactory;

    private MessageFactory messageFactory;

    private ContextFactoryBuilder policyBuilder;

    public JbiServiceEntryPointBuilder() {
    }

    @Init(eager = true)
    public void init() {
        runtimeContext.addBuilder(this);
    }

    /**
     * @param runtimeContext The runtimeContext to set.
     */
    @Autowire
    public void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    /**
     * Sets the factory used to construct proxies implmementing the business interface required by a reference
     */
    @Autowire
    public void setProxyFactoryFactory(ProxyFactoryFactory factory) {
        this.proxyFactoryFactory = factory;
    }

    /**
     * Sets the factory used to construct invocation messages
     * 
     * @param msgFactory
     */
    @Autowire
    public void setMessageFactory(MessageFactory msgFactory) {
        this.messageFactory = msgFactory;
    }

    /**
     * Sets a builder responsible for creating source-side and target-side invocation chains for a reference. The
     * reference builder may be hierarchical, containing other child reference builders that operate on specific
     * metadata used to construct and invocation chain.
     * 
     * @see org.apache.tuscany.core.builder.impl.HierarchicalBuilder
     */
    public void setPolicyBuilder(ContextFactoryBuilder builder) {
        policyBuilder = builder;
    }

    public void build(AssemblyModelObject object) throws BuilderException {
        if (!(object instanceof EntryPoint)) {
            return;
        }
        EntryPoint entryPoint = (EntryPoint) object;
        if (entryPoint.getBindings().size() < 1 || !(entryPoint.getBindings().get(0) instanceof JbiBinding)) {
            return;
        }

        EntryPointContextFactory config = new JbiServiceEntryPointContextFactory(entryPoint.getName(), entryPoint.getConfiguredService().getService().getName(), messageFactory);

        ConfiguredService configuredService = entryPoint.getConfiguredService();
        Service service = configuredService.getService();
        ServiceContract serviceContract = service.getServiceContract();
        Map<Method, InvocationConfiguration> iConfigMap = new HashMap<Method, InvocationConfiguration>();
        ProxyFactory proxyFactory = proxyFactoryFactory.createProxyFactory();
        Set<Method> javaMethods = JavaIntrospectionHelper.getAllUniqueMethods(serviceContract.getInterface());
        for (Method method : javaMethods) {
            InvocationConfiguration iConfig = new InvocationConfiguration(method);
            iConfigMap.put(method, iConfig);
        }
        QualifiedName qName = new QualifiedName(entryPoint.getConfiguredReference().getTargetConfiguredServices().get(0).getAggregatePart().getName() + "/" + service.getName());
        ProxyConfiguration pConfiguration = new ProxyConfiguration(qName, iConfigMap, serviceContract.getInterface().getClassLoader(), messageFactory);
        proxyFactory.setBusinessInterface(serviceContract.getInterface());
        proxyFactory.setProxyConfiguration(pConfiguration);
        config.addSourceProxyFactory(service.getName(), proxyFactory);
        configuredService.setProxyFactory(proxyFactory);
        if (policyBuilder != null) {
            // invoke the reference builder to handle additional policy metadata
            policyBuilder.build(configuredService);
        }
        // add tail interceptor
        for (InvocationConfiguration iConfig : (Collection<InvocationConfiguration>) iConfigMap.values()) {
            iConfig.addTargetInterceptor(new EntryPointInvokerInterceptor());
        }
        entryPoint.getConfiguredReference().setContextFactory(config);
    }
    
    //FIXME same as the InvokerInterceptor except that it doesn't throw an exception in setNext
    // For some reason another InvokerInterceptor is added after this one, need Jim to look into it
    // and figure out why.
    public class EntryPointInvokerInterceptor implements Interceptor {
        
        public EntryPointInvokerInterceptor() {
        }

        public Message invoke(Message msg) throws InvocationRuntimeException {
            TargetInvoker invoker = msg.getTargetInvoker();
            if (invoker == null) {
                throw new InvocationRuntimeException("No target invoker specified on message");
            }
            return invoker.invoke(msg);
        }

        public void setNext(Interceptor next) {
        }

    }

}
