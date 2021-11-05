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

package org.springframework.aot.context.bootstrap.generator.sample.injection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
@SuppressWarnings("unused")
public class InjectionConfiguration {

	@Bean
	InjectionComponent injectionComponent() {
		return new InjectionComponent("test");
	}

	@Autowired
	public void setEnvironment(Environment environment) {

	}

	@Autowired(required = false)
	public void setBean(String bean) {

	}

	public void setName(String name) {

	}

	public void setCounter(Integer counter) {

	}

	public void setNames(List<String> names) {

	}

}
