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

package org.springframework.context.bootstrap.generator.bean.support;

import static org.assertj.core.api.Assertions.*;

import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.PrivateExternallyManagedInitMethod;
import org.springframework.context.bootstrap.generator.sample.callback.ConfigHavingBeansWithInitMethod.PublicExternallyManagedInitMethod;
import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 */
class MethodInvocationWriterTests {

	@Test // GH-1048
	void codeBlockForPublicMethod() {

		CodeBlock codeBlock = MethodInvocationWriter.writeMethodInvocationOn("bean", ReflectionUtils.findMethod(PublicExternallyManagedInitMethod.class, "publicInit"));
		assertThat(CodeSnippet.of(codeBlock)).lines().containsExactly("bean.publicInit();");
	}
	@Test // GH-1048
	void codeBlockForPrivateMethod() {

		CodeBlock codeBlock = MethodInvocationWriter.writeMethodInvocationOn("bean", ReflectionUtils.findMethod(PrivateExternallyManagedInitMethod.class, "privateInit"));
		assertThat(CodeSnippet.of(codeBlock)).lines()
				.containsExactly(
						"Method privateInitMethod = ReflectionUtils.findMethod(ConfigHavingBeansWithInitMethod.PrivateExternallyManagedInitMethod.class, \"privateInit\");", //
						"ReflectionUtils.makeAccessible(privateInitMethod);", //
						"ReflectionUtils.invokeMethod(privateInitMethod, bean);" //
				);
	}
}
