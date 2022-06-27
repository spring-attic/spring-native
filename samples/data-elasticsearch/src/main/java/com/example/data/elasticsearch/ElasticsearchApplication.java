/*
 * Copyright 2012-2021 the original author or authors.
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
package com.example.data.elasticsearch;

import org.springframework.aot.thirdpartyhints.NettyRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;

@SpringBootApplication
@EnableElasticsearchAuditing(auditorAwareRef = "fixedAuditor")
@ImportRuntimeHints(NettyRuntimeHints.class)
public class ElasticsearchApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication app = new SpringApplication(ElasticsearchApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run();
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

	@Bean
	AuditorAware<String> fixedAuditor() {
		return () -> java.util.Optional.of("Douglas Adams");
	}
}
