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

package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration", innerClass = "CacheConfigurationImportSelector", onlyWith = OnlyIfPresent.class)
final class Target_CacheConfigurationImportSelector {

	// TODO Import dynamically the required caching configuration as described in https://github.com/spring-projects-experimental/spring-native/issues/465
	@Substitute
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		return new String[0];
	}
}
