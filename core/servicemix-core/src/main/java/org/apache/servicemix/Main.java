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
package org.apache.servicemix;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.xbean.ClassLoaderXmlPreprocessor;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.xbean.spring.context.FileSystemXmlApplicationContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * A simple stand alone application which runs ServiceMix from the command line.
 *
 * @version $Revision$
 */
public final class Main {
    
    private Main() {
    }

    public static void main(String args[]) {
        try {
            String version = "";
            Package p = Package.getPackage("org.apache.servicemix");
            if (p != null) {
                version = ": " + p.getImplementationVersion();
            }
            System.out.println("Starting Apache ServiceMix ESB" + version);
            System.out.println();

            final AbstractXmlApplicationContext context;
            if (args.length <= 0) {
                System.out.println("Loading Apache ServiceMix from servicemix.xml on the CLASSPATH");
                context = new ClassPathXmlApplicationContext(new String[] {"servicemix.xml"}, false);
            } else {
                String file = args[0];

                if ("-?".equals(file) || "?".equals(file) || "--help".equals(file) || "-h".equals(file)) {
                    System.out.println("Usage: Main [-v1] [xmlConfigFile]");
                    System.out.println("If an XML config file is not specified then servicemix.xml is used from the CLASSPATH");
                    return;
                }
                
                List processors = new ArrayList();
                processors.add(new ClassLoaderXmlPreprocessor(new File(".")));
                System.out.println("Loading Apache ServiceMix from file: " + file);
                context = new FileSystemXmlApplicationContext(new String[] {file}, false, processors);
            }
            context.setValidating(false);
            context.refresh();
            
            SpringJBIContainer container = (SpringJBIContainer) context.getBean("jbi");            
            container.onShutDown(new Runnable() {
                public void run() {
                    if (context instanceof DisposableBean) {
                        try {
                            ((DisposableBean) context).destroy();
                        } catch (Exception e) {
                            System.out.println("Caught: " + e);
                            e.printStackTrace();
                        }
                    }
                }
            });
            //this is for classworlds 1.1 launcher which use System.exit() 
            //explicitly after lauch Main. To avoid System.exit() being invoked
            //during servicemix runing, we need keep ServiceMix main thread alive.
            container.block();
            
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
}
