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

package org.springframework.aot.factories;

import com.squareup.javapoet.ClassName;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;

/**
 * {@link FactoriesCodeContributor} that contributes source code for
 * {@link org.springframework.test.context.TestExecutionListener} implementations.
 *
 * <p>Instead of instantiating them statically, we make sure that
 * {@link org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}
 * will return their names and that reflection metadata is registered for native images.
 *
 * <p>In addition, this {@code FactoriesCodeContributor} transparently replaces the
 * {@code DependencyInjectionTestExecutionListener} from {@code spring-test} with
 * the {@code AotDependencyInjectionTestExecutionListener} from {@code spring-native}.
 *
 * @author Sam Brannen
 * @author Brian Clozel
 * @see org.springframework.nativex.substitutions.framework.test.Target_AbstractTestContextBootstrapper
 * @see org.springframework.test.context.support.AbstractTestContextBootstrapper.instantiateListeners
 */
class TestExecutionListenerFactoriesCodeContributor implements FactoriesCodeContributor {

	private static final String TEL = "org.springframework.test.context.TestExecutionListener";

	private static final String DITEL = "org.springframework.test.context.support.DependencyInjectionTestExecutionListener";

	private static final String AOT_DITEL = "org.springframework.aot.test.AotDependencyInjectionTestExecutionListener";


	@Override
	public boolean canContribute(SpringFactory factory) {
		return TEL.equals(factory.getFactoryType().getName());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		String className = factory.getFactory().getName();

		// Replace standard DITEL with the AOT variant.
		String factoryClassName = (DITEL.equals(className) ? AOT_DITEL : className);
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getName());

		generateReflectionMetadata(factoryClassName, context);
		code.writeToStaticBlock(builder -> {
			builder.addStatement("names.add($T.class, $S)", factoryTypeClass, factoryClassName);
		});
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		ClassDescriptor classDescriptor = ClassDescriptor.of(factoryClassName);
		classDescriptor.addMethodDescriptor(MethodDescriptor.defaultConstructor());
		context.describeReflection(reflect -> reflect.add(classDescriptor));
	}

}
