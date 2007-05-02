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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import junit.framework.TestCase;

public class JdbcComponentUniqueNamesTest extends TestCase {

	public void testNames() throws Exception {

		JdbcComponent comp = new JdbcComponent();

		String[] names = new String[]{"name", "name", "name", "id", "test_1", "test_1", "test_1"};
		
		ResultSetMetaData metaData = new MetaMock(names);
		String[] uniqueColumnNames = comp.getUniqueColumnNames(metaData);
		
		String[] expected = 
			new String[]{
				"name", 
				"name_1", 
				"name_2", 
				"id", 
				"test_1", 
				"test_1_1", 
				"test_1_2"};
		
		for (int i = 0; i < uniqueColumnNames.length; i++) {
			assertEquals(expected[i], uniqueColumnNames[i]);
		}
	}

	private static class MetaMock implements ResultSetMetaData {

		private final String[] names;
		
		MetaMock(String[] names) {
			this.names = names;
		}
		
		public int getColumnCount() throws SQLException {
			return names.length;
		}

		public String getColumnName(int column) throws SQLException {
			return names[column-1];
		}

		public String getCatalogName(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public String getColumnClassName(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public int getColumnDisplaySize(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public String getColumnLabel(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public int getColumnType(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public String getColumnTypeName(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public int getPrecision(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public int getScale(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public String getSchemaName(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public String getTableName(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isAutoIncrement(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isCaseSensitive(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isCurrency(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isDefinitelyWritable(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public int isNullable(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isReadOnly(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isSearchable(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isSigned(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isWritable(int column) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public boolean isWrapperFor(Class arg0) throws SQLException {
			throw new RuntimeException("not implemented");
		}

		public Object unwrap(Class arg0) throws SQLException {
			throw new RuntimeException("not implemented");
		}

	}

}
