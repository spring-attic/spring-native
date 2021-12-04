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

package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Sam Brannen
 */
@SpringJUnitConfig
@TestMethodOrder(MethodName.class)
class DirtiesContextTests {

	@BeforeAll
	@AfterAll
	static void resetCounter() {
		Config.counter.set(0);
	}

	@Test
	void test1(@Autowired String foo) {
		assertThat(foo).isEqualTo("bar1");
	}

	@Test
	@DirtiesContext
	void test2(@Autowired String foo) {
		// foo should still be bar1 due to context caching
		assertThat(foo).isEqualTo("bar1");
	}

	@Test
	void test3(@Autowired String foo) {
		// foo should be bar2 due to @DirtiesContext on test2()
		assertThat(foo).isEqualTo("bar2");
	}

	@Configuration
	static class Config {

		static final AtomicInteger counter = new AtomicInteger();

		@Bean
		String foo() {
			return "bar" + counter.incrementAndGet();
		}
	}

}
