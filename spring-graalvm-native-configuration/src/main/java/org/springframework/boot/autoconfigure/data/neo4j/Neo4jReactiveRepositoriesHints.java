package org.springframework.boot.autoconfigure.data.neo4j;

import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension;
import org.springframework.data.neo4j.repository.event.ReactiveBeforeBindCallback;
import org.springframework.data.neo4j.repository.event.ReactiveIdGeneratingBeforeBindCallback;
import org.springframework.data.neo4j.repository.event.ReactiveOptimisticLockingBeforeBindCallback;
import org.springframework.data.neo4j.repository.support.SimpleReactiveNeo4jRepository;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger = Neo4jReactiveRepositoriesAutoConfiguration.class, typeInfos = {
	@TypeInfo(types = {
		ReactiveNeo4jRepository.class,
		ReactiveNeo4jRepositoryConfigurationExtension.class,
		AbstractReactiveNeo4jConfig.class,
		SimpleReactiveNeo4jRepository.class,
		ReactiveNeo4jTemplate.class,
		ReactiveNeo4jClient.class,
		ReactiveBeforeBindCallback.class,
		ReactiveIdGeneratingBeforeBindCallback.class,
		ReactiveOptimisticLockingBeforeBindCallback.class,

		UUIDStringGenerator.class
	},
		typeNames = {
			"org.springframework.data.neo4j.repository.support.SimpleQueryByExampleExecutor",
			"org.springframework.data.neo4j.repository.support.SimpleReactiveQueryByExampleExecutor",

			"org.springframework.data.neo4j.core.schema.GeneratedValue$InternalIdGenerator",
			"org.springframework.data.neo4j.core.schema.GeneratedValue$UUIDGenerator"
		}
	) },
	proxyInfos = {
		@ProxyInfo(typeNames = {
			"org.springframework.data.annotation.Id",
			"org.springframework.core.annotation.SynthesizedAnnotation"
		})
	})
public class Neo4jReactiveRepositoriesHints implements NativeImageConfiguration {

}
