/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.sample.data.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.RepositoryDefinitionConfigurationProcessorTests;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.repository.Repository;
import org.springframework.sample.data.types.Customer;
import org.springframework.sample.data.types.DomainObjectWithSimpleTypesOnly;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Strobl
 */
@Configuration
@EnableMongoRepositories(considerNestedRepositories = true, includeFilters = {@Filter(type = FilterType.REGEX, pattern = ".*RepositoryWithCustomImplementation")})
public class ConfigWithCustomImplementation {

	public interface RepositoryWithCustomImplementation extends Repository<DomainObjectWithSimpleTypesOnly, String>, CustomImplInterface {

	}

	public interface CustomImplInterface {

		List<Customer> findMyCustomer();
	}

	@Component
	public static class RepositoryWithCustomImplementationImpl implements CustomImplInterface {

		@Override
		public List<Customer> findMyCustomer() {
			return Collections.emptyList();
		}
	}
}
