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
package org.apache.servicemix.jbi.audit.file;

import java.io.File;
import java.util.Locale;

import javax.jbi.messaging.InOnly;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;

public class FileAuditorTest extends TestCase {

    private static final File DIRECTORY = new File("target/tests/FileAuditor");

    private JBIContainer jbi;

    protected void setUp() throws Exception {
        jbi = new JBIContainer();
        jbi.setFlowName("st");
        jbi.setEmbedded(true);
        jbi.init();
        FileUtil.deleteFile(DIRECTORY); 
        DIRECTORY.mkdirs();
    }

    protected void tearDown() throws Exception {
        if (jbi != null) {
            jbi.shutDown();
        }
    }

    public void testFileAuditor() throws Exception {
        jbi.start();
        SenderComponent sender = new SenderComponent();
        ReceiverComponent receiver = new ReceiverComponent();
        jbi.activateComponent(sender, "sender");
        jbi.activateComponent(receiver, "receiver");

        FileAuditor auditor = new FileAuditor();
        auditor.setContainer(jbi);
        auditor.setDirectory(DIRECTORY);
        auditor.afterPropertiesSet();

        InOnly inonly = sender.createInOnlyExchange(ReceiverComponent.SERVICE, null, null);
        inonly.setInMessage(inonly.createMessage());
        inonly.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        inonly.getInMessage().setProperty("from", Locale.getDefault().getCountry());
        sender.send(inonly);

        //check if a message has been audited
        int nbMessages = auditor.getExchangeCount();
        assertEquals(1, nbMessages);
    }

}
