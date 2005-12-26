/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix;

import java.util.Collections;
import java.util.List;

import org.apache.servicemix.jbi.config.spring.XBeanProcessor;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.xbean.spring.context.ClassPathXmlApplicationContext;
import org.xbean.spring.context.FileSystemXmlApplicationContext;

/**
 * A simple stand alone application which runs ServiceMix from the command line.
 *
 * @version $Revision$
 */
public class Main {

    public static void main(String args[]) {
        try {
            String version = "";
            Package p = Package.getPackage("org.apache.servicemix");
            if (p != null) {
                version = ": " + p.getImplementationVersion();
            }
            System.out.println("ServiceMix ESB" + version);
            System.out.println();

            ApplicationContext context = null;
            if (args.length <= 0) {
                System.out.println("Loading ServiceMix from servicemix.xml on the CLASSPATH");
                context = new ClassPathXmlApplicationContext("servicemix.xml");
            }
            else {
                String file = args[0];

                if (file.equals("-?") || file.equals("?") || file.equals("--help") || file.equals("-h")) {
                    System.out.println("Usage: Main [-v1] [xmlConfigFile]");
                    System.out.println("If an XML config file is not specified then servicemix.xml is used from the CLASSPATH");
                    return;
                }
                
                if (file.equals("-v1")) {
                    
                    List processors = Collections.singletonList(new XBeanProcessor());
                    if (args.length <= 1) {
                        System.out.println("Loading ServiceMix (compatible 1.x) from servicemix.xml on the CLASSPATH");
                        context = new ClassPathXmlApplicationContext("servicemix.xml", processors);
                    }
                    else {
                        file = args[1];
                        System.out.println("Loading ServiceMix (compatible 1.x) from file: " + file);
                        context = new FileSystemXmlApplicationContext(file, processors);
                    }
                }
                else {
                    System.out.println("Loading ServiceMix from file: " + file);
                    context = new FileSystemXmlApplicationContext(file);
                }
            }
            SpringJBIContainer container = (SpringJBIContainer) context.getBean("jbi");
            Object lock = new Object();
            container.setShutdownLock(lock);

            // lets wait until we're killed.
            synchronized (lock) {
                lock.wait();
            }
            if (context instanceof DisposableBean) {
                ((DisposableBean) context).destroy();
            }
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
}
