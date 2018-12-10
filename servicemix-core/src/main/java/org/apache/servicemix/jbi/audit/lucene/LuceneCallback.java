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

import org.apache.lucene.search.IndexSearcher;

/**
 * Lucene Callback Interface. 
 * Used on searching to be executed on synchronized blocks. 
 * @author George Gastaldi
 * @since 2.1
 * @version $Revision$
 */
public interface LuceneCallback {
	/**
	 * Called by the LuceneIndexer 
	 * @param is IndexSearcher provided by the indexer
	 * @return an object from the query
	 * @throws IOException if an error occurs during opening/searching of the index
	 */	
	Object doCallback(IndexSearcher is) throws IOException;
}
