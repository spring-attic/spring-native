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

package org.springframework.nativex.type;

import java.util.List;

public interface ComponentProcessor {

	/**
	 * Does this processor want to process the specified key/values.
	 * Examples:
	 * <pre><code>
	 * componentType=app.main.SampleApplication classifiers=[org.springframework.stereotype.Component]
	 * componentType=app.main.model.Foo classifiers=[javax.persistence.Entity]
	 * componentType=app.main.model.FooRepository classifiers=[org.springframework.data.repository.Repository]
	 * </code></pre>
	 * @param imageContext the image context
	 * @param componentType the component type
	 * @param classifiers the classifier
	 * @return {@code true} if processing should be done
	 */
	boolean handle(NativeContext imageContext, String componentType, List<String> classifiers);

	void process(NativeContext imageContext, String componentType, List<String> classifiers);

	default void printSummary() {}
}
