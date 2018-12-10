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
package org.apache.servicemix.jbi.audit.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

/**
 * Default Lucene Callback implementation. Used on LuceneAuditor
 * @author George Gastaldi (gastaldi)
 * @since 2.1
 * @version $Revision$
 */
public class DefaultLuceneCallback implements LuceneCallback {
	private String field;
	private String query;
	
	public DefaultLuceneCallback(String field, String query) {
		this.field = field;
		this.query = query;
	}
	
	public Object doCallback(IndexSearcher is) throws IOException {
		try {
			Query queryObj = QueryParser.parse(query, field, new StandardAnalyzer());
			Hits hits = is.search(queryObj);
			int total = hits.length();
			String[] ids = new String[total];
			for (int i = 0; i < total; i++) {
				Document d = hits.doc(i);
				ids[i] = d.get("org.apache.servicemix.exchangeid");
			}
			return ids;
		} catch (ParseException pe) {
			return new String[0];
		}
	}

}
