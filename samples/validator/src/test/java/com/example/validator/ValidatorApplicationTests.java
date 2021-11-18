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

package com.example.validator;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ValidatorApplicationTests {

	@Autowired(required = false)
	private Validator validator;

	@Autowired
	private GenericApplicationContext applicationContext;

	@Test
	void validatorHasPrimaryFlag() {
		assertThat(this.validator).isNotNull();
		assertThat(this.applicationContext.containsBeanDefinition("defaultValidator")).isTrue();
		assertThat(this.applicationContext.getBeanDefinition("defaultValidator").isPrimary()).isTrue();
	}

	@Test
	void twoValidatorsAreDefined() {
		assertThat(this.applicationContext.getBeansOfType(Validator.class))
				.containsOnlyKeys("defaultValidator", "mvcValidator");
	}

}
