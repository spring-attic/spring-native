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
package app.main;

import java.util.List;
import java.util.Optional;

import app.main.model.Flurb;
import app.main.model.Foo;
import app.main.model.FooRepository;
import org.springframework.aot.thirdpartyhints.HikariRuntimeHints;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.web.servlet.function.RouterFunction;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "fixedAuditor")
@EnableJpaRepositories(basePackageClasses = FooRepository.class)
@ImportRuntimeHints({HikariRuntimeHints.class, RuntimeHints.class})
public class SampleApplication {

	private final FooRepository entities;

	public SampleApplication(FooRepository entities) {
		this.entities = entities;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {

			Optional<Foo> maybeFoo = entities.findById(1L);
			Foo foo;
			foo = maybeFoo.orElseGet(() -> entities.save(new Foo("Hello")));
			Flurb flurb = new Flurb();
			flurb.setVal("Balla balla");
			foo.setFlurb(flurb);
			entities.save(foo);

			List<Foo> withBetween = entities.findWithBetween("a", "X");
			System.out.println("withBetween: " + withBetween);
			System.out.println(entities.findById(1L));
			System.out.println("DONE");
		};
	}

	@Bean
	AuditorAware<String> fixedAuditor() {
		return () -> Optional.of("Douglas Adams");
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(findOne()));
	}

	private Foo findOne() {
		return entities.findById(1L).get();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
