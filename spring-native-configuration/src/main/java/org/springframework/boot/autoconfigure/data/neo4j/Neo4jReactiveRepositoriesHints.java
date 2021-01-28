package org.springframework.boot.autoconfigure.data.neo4j;

import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.mapping.callback.ReactiveAuditingBeforeBindCallback;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension;
import org.springframework.data.neo4j.repository.support.Neo4jEvaluationContextExtension;
import org.springframework.data.neo4j.repository.support.SimpleReactiveNeo4jRepository;
import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.ProxyInfo;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeHint(trigger = Neo4jReactiveRepositoriesAutoConfiguration.class, typeInfos = {
	@TypeInfo(types = {
		org.springframework.data.neo4j.core.mapping.callback.ReactiveBeforeBindCallback.class,
		org.springframework.data.neo4j.repository.event.ReactiveBeforeBindCallback.class,
	}, access= AccessBits.CLASS|AccessBits.DECLARED_METHODS),
	@TypeInfo(types = {
		ReactiveNeo4jRepository.class,
		ReactiveNeo4jRepositoryConfigurationExtension.class,
		AbstractReactiveNeo4jConfig.class,
		SimpleReactiveNeo4jRepository.class,
		ReactiveNeo4jTemplate.class,
		ReactiveNeo4jClient.class,
		ReactiveAuditingBeforeBindCallback.class,
		UUIDStringGenerator.class,
		Neo4jEvaluationContextExtension.class
	},
		typeNames = {
			"org.springframework.data.neo4j.repository.query.SimpleQueryByExampleExecutor",
			"org.springframework.data.neo4j.repository.query.SimpleReactiveQueryByExampleExecutor",
			"org.springframework.data.neo4j.core.schema.GeneratedValue$InternalIdGenerator",
			"org.springframework.data.neo4j.core.schema.GeneratedValue$UUIDGenerator",
			"org.springframework.data.neo4j.core.mapping.callback.ReactiveIdGeneratingBeforeBindCallback",
			"org.springframework.data.neo4j.core.mapping.callback.ReactiveOptimisticLockingBeforeBindCallback"
		}
	) },
	proxyInfos = {
		@ProxyInfo(typeNames = {
			"org.springframework.data.annotation.Id",
			"org.springframework.core.annotation.SynthesizedAnnotation"
		})
	})
public class Neo4jReactiveRepositoriesHints implements NativeConfiguration {

}
