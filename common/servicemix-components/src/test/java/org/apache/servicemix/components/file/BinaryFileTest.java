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
package org.apache.servicemix.components.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class BinaryFileTest extends TestSupport {

	protected void setUp() throws Exception {
		FileUtil.deleteFile(new File("target/test-data/in"));
		FileUtil.deleteFile(new File("target/test-data/out"));
		super.setUp();
	}
	
    public void testSendBinary() throws Exception {
    	String contents = "Binary content";
        FileWriter fw = new FileWriter("target/test-data/in/file.txt");
        fw.write(contents);
        fw.close();
        
        File output = null;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000) {
        	File outDir = new File("target/test-data/out");
        	File[] files = outDir.listFiles();
        	if (files != null && files.length > 0) {
        		output = files[0];
        		break;
        	}
        	Thread.sleep(50);
        }
        if (output == null) {
        	fail("No output file found");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copyInputStream(new FileInputStream(output), baos);
        String outContents = baos.toString();
        assertEquals(contents, outContents);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/file/binary-example.xml");
    }

}
