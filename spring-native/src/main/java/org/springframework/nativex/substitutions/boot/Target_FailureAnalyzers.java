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

import java.util.List;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.WithAot;

/**
 * Why this substitution exists?
 * To avoid using SpringFactoriesLoader#loadFactoryNames() in order to use reflection-less variant when possible.
 *
 * How this substitution workarounds the problem?
 * It invokes SpringFactoriesLoader#loadFactories instead (which is using underneath StaticSpringFactories generated AOT).
 */
@TargetClass(className="org.springframework.boot.diagnostics.FailureAnalyzers", onlyWith = { WithAot.class, OnlyIfPresent.class })
final class Target_FailureAnalyzers {

	@Substitute
	private List<FailureAnalyzer> loadFailureAnalyzers(ConfigurableApplicationContext context,
			ClassLoader classLoader) {
		// TODO: analyzers are not ordered
		return SpringFactoriesLoader.loadFactories(FailureAnalyzer.class, classLoader);
	}

}
