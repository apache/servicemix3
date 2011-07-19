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
package org.apache.servicemix.components.vfs;

import java.io.File;

import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import javax.xml.namespace.QName;

/**
 * @version $Revision$
 */
public class FileTest extends TestSupport {

    private static final String SYSTEM_OS_KEY = "os.name";
    private static final String WINDOWS = "Windows";

	protected void setUp() throws Exception {
		super.setUp();
		if (isWindowsOS()) {
			FileUtil.deleteFile(new File("c:/tmp/servicemix/vfs/test"));
		} else {
			FileUtil.deleteFile(new File("/tmp/servicemix/vfs/test"));
		}
	}
	
    public void testSendMessagesToFileSystemThenPoollThem() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "fileSender");
        assertSendAndReceiveMessages(service);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(getSystemSupportedXMLConfig());
    }

    protected String getSystemSupportedXMLConfig(){
        if (isWindowsOS()) {
            return "org/apache/servicemix/components/vfs/example_win.xml";
        } else {
            return "org/apache/servicemix/components/vfs/example.xml";
        }
    }
    
    protected boolean isWindowsOS() {
        String os = System.getProperty(SYSTEM_OS_KEY);
        boolean isWindows = false;
        int index = (os==null?-1:os.indexOf(WINDOWS));
        if(index > -1 ){
            isWindows = true;
        }
        return isWindows;
    }

}
