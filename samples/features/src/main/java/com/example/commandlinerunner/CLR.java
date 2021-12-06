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

package com.example.commandlinerunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	public FooBean foo;

	@Bean
	SomeComponent someComponent() {
		return new SomeComponent();
	}

	@Autowired
	SomeBean someBean;

	@Autowired
	ApplicationContext env;

	@Override
	public void run(String... args) throws Exception {
		if (foo == null) {
			throw new IllegalStateException("foo is not set");
		}
		// Verifying that even though SomeComponent is not marked @Component the
		// field inside gets autowired
		env.getBean(SomeComponent.class).check();
		System.out.println("commandlinerunner running!");
	}

}

class SomeComponent {
	@Autowired Environment field;

	public void check() {
		if (field == null) {
			throw new IllegalStateException("SomeComponent.field is null");
		}
	}
}
