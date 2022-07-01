/*
 * Copyright 2012-2022 the original author or authors.
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

package com.example.data.cassandra;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.config.EnableCassandraAuditing;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.cassandra.core.cql.generator.CreateKeyspaceCqlGenerator;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.ManagedTypes;

@SpringBootApplication
@EnableCassandraAuditing(auditorAwareRef = "fixedAuditor")
public class CassandraApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication.run(CassandraApplication.class);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@Bean
	AuditorAware<String> fixedAuditor() {
		return () -> Optional.of("Douglas Adams");
	}

	@Bean // TODO: ManagedTypes should happen in Spring Boot
	public CassandraMappingContext cassandraMappingContext(BeanFactory beanFactory, CassandraCustomConversions conversions, ManagedTypes managedTypes) throws ClassNotFoundException {

		CassandraMappingContext context = new CassandraMappingContext();
		context.setManagedTypes(managedTypes);
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		return context;
	}

	@Bean
	CqlSessionBuilderCustomizer keyspaceCreator() {
		return cqlSessionBuilder -> {

			cqlSessionBuilder.withKeyspace("system");
			CqlSession session = cqlSessionBuilder.build();

			Map<CqlIdentifier, KeyspaceMetadata> keyspaces = session.getMetadata()
					.getKeyspaces();
			if (!keyspaces.containsKey(CqlIdentifier.fromCql("aot"))) {


				String cql = CreateKeyspaceCqlGenerator.toCql(CreateKeyspaceSpecification.createKeyspace("aot")
						.withSimpleReplication());

				session.execute(cql);
			}

			session.close();

			cqlSessionBuilder.withKeyspace("aot");
		};
	}

	@Bean("cassandraManagedTypes")
	ManagedTypes managedTypes() {
		return ManagedTypes.fromIterable(Collections.singleton(Order.class));
	}

}
