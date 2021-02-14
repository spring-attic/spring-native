/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.boot.autoconfigure.data;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;

@NativeHint(trigger = CassandraReactiveRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = CassandraRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = CouchbaseReactiveRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = CouchbaseRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = MongoReactiveRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = MongoRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = Neo4jReactiveRepositoriesAutoConfiguration.class, typeInfos= {
	@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = Neo4jRepositoriesAutoConfiguration.class, typeInfos= {
	@TypeInfo(types= {ConditionalOnRepositoryType.class, OnRepositoryTypeCondition.class,RepositoryType.class})
})
@NativeHint(trigger = AbstractRepositoryConfigurationSourceSupport.class, typeInfos = {
	// TODO who else needs PropertiesFactoryBean? It can't just be data related things can it...
	// TODO I've made PFB globally accessible as vanilla-jpa sample needed it and wasn't seeing it through this AbstractRepo config
	@TypeInfo(types = {PropertiesFactoryBean.class, PropertiesLoaderSupport.class /* super of PropertiesFactoryBean*/}) })
public class RepositoriesHints implements NativeConfiguration {
}
