/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.graalvm.extension;

import java.util.List;

public interface ComponentProcessor {

	/**
	 * Does this processor want to process the specified key/values.
	 * Examples:
	 * <pre><code>
	 * key=app.main.SampleApplication values=[org.springframework.stereotype.Component]
	 * key=app.main.model.Foo values=[javax.persistence.Entity]
	 * key=app.main.model.FooRepository values=[org.springframework.data.repository.Repository]
	 * </code></pre> 
	 */
	boolean handle(String key, List<String> values);

	void process(NativeImageContext imageContext, String key, List<String> values);

}
