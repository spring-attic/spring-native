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

package org.springframework.aot.test.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * An extension of {@link SpringBootContextLoader} that creates a
 * {@link SpringApplication} that is suitable for build-time processing.
 *
 * @author Stephane Nicoll
 */
class SpringBootBuildTimeConfigContextLoader extends SpringBootContextLoader {

	@Override
	public GenericApplicationContext loadContext(MergedContextConfiguration config) throws Exception {
		return (GenericApplicationContext) super.loadContext(config);
	}

	@Override
	protected SpringApplication getSpringApplication() {
		return new BuildTimeTestSpringApplication();
	}

}
