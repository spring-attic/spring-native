package org.springframework.boot.autoconfigure.data.neo4j;

import org.springframework.data.neo4j.config.AbstractNeo4jConfig;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import org.springframework.data.neo4j.repository.config.Neo4jRepositoryConfigurationExtension;
import org.springframework.data.neo4j.repository.event.BeforeBindCallback;
import org.springframework.data.neo4j.repository.event.IdGeneratingBeforeBindCallback;
import org.springframework.data.neo4j.repository.event.OptimisticLockingBeforeBindCallback;
import org.springframework.data.neo4j.repository.support.Neo4jRepositoryFactoryBean;
import org.springframework.data.neo4j.repository.support.SimpleNeo4jRepository;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger = Neo4jRepositoriesAutoConfiguration.class, typeInfos = {
	@TypeInfo(types = {
		Neo4jRepositoryFactoryBean.class,
		Neo4jRepositoryConfigurationExtension.class,
		AbstractNeo4jConfig.class,
		SimpleNeo4jRepository.class,
		Neo4jTemplate.class,
		Neo4jClient.class,
		BeforeBindCallback.class,
		IdGeneratingBeforeBindCallback.class,
		OptimisticLockingBeforeBindCallback.class,

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
public class Neo4jRepositoriesHints implements NativeImageConfiguration {

}
