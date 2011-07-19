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
package org.apache.servicemix.components.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import javax.jbi.messaging.InOut;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.hsqldb.jdbc.jdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcComponentTest extends TestCase {

    private static transient Logger logger = LoggerFactory.getLogger(JdbcComponentTest.class);

    private JBIContainer jbi;
	private DataSource dataSource;
	private JdbcComponent jdbc;
	
	protected void setUp() throws Exception {
        jdbcDataSource ds = new jdbcDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:aname");
        ds.setUser("sa");
		dataSource = ds;
		
		jbi = new JBIContainer();
		jbi.setEmbedded(true);
		jbi.init();
		jbi.start();
		
		jdbc = new JdbcComponent();
		jdbc.setService(new QName("urn:jdbc", "service"));
		jdbc.setEndpoint("endpoint");
		jdbc.setDataSource(dataSource);
		jbi.activateComponent(jdbc, "jdbc");
	}
	
	protected void tearDown() throws Exception {
		jbi.shutDown();
	}

	public void testInOut() throws Exception {
		Connection con = dataSource.getConnection("sa", "");
		Statement st = con.createStatement();
		st.execute("create table MyTable (id varchar(80) not null, name varchar(80))");
		st.execute("insert into MyTable values ('1', 'One')");
		st.execute("insert into MyTable values ('2', 'Two')");
		
		DefaultServiceMixClient client = new DefaultServiceMixClient(jbi);
		InOut me = client.createInOutExchange();
		me.setService(new QName("urn:jdbc", "service"));
		me.getInMessage().setContent(new StringSource("<sql>select * from MyTable</sql>"));
		client.sendSync(me);
		String out = new SourceTransformer().contentToString(me.getOutMessage());
		logger.info(out);
		assertTrue(out.contains("One"));
		assertTrue(out.contains("Two"));
	}

}
