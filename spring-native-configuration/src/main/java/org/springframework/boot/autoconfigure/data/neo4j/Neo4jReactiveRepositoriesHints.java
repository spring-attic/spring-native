/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.data.neo4j;

import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.mapping.callback.ReactiveAuditingBeforeBindCallback;
import org.springframework.data.neo4j.core.mapping.callback.ReactiveBeforeBindCallback;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension;
import org.springframework.data.neo4j.repository.support.Neo4jEvaluationContextExtension;
import org.springframework.data.neo4j.repository.support.ReactiveNeo4jRepositoryFactoryBean;
import org.springframework.data.neo4j.repository.support.SimpleReactiveNeo4jRepository;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

@NativeHint(trigger = ReactiveNeo4jRepositoryFactoryBean.class,
		options = "--enable-https", types = {
	@TypeHint(types = {
			SimpleReactiveNeo4jRepository.class,
			ReactiveNeo4jRepository.class,
			ReactiveAuditingBeforeBindCallback.class,
			ReactiveBeforeBindCallback.class
	}, typeNames = {
			"org.springframework.data.neo4j.repository.query.SimpleQueryByExampleExecutor",
			"org.springframework.data.neo4j.repository.query.SimpleReactiveQueryByExampleExecutor",
			"org.springframework.data.neo4j.core.mapping.callback.ReactiveIdGeneratingBeforeBindCallback",
			"org.springframework.data.neo4j.core.mapping.callback.ReactiveOptimisticLockingBeforeBindCallback"
	}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
	@TypeHint(types = {
		ReactiveNeo4jRepositoryConfigurationExtension.class,
		AbstractReactiveNeo4jConfig.class,
		ReactiveNeo4jTemplate.class,
		ReactiveNeo4jClient.class,
		UUIDStringGenerator.class,
		Neo4jEvaluationContextExtension.class
	}, typeNames = {
			"org.springframework.data.neo4j.core.schema.GeneratedValue$InternalIdGenerator",
			"org.springframework.data.neo4j.core.schema.GeneratedValue$UUIDGenerator",
		}
	)},
	jdkProxies = {
		@JdkProxyHint(typeNames = {
			"org.springframework.data.annotation.Id",
			"org.springframework.core.annotation.SynthesizedAnnotation"
		})
	})
public class Neo4jReactiveRepositoriesHints implements NativeConfiguration {

}
