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
 * <p>Instead of instantiating them statically, we make sure that
 * {@link org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}
 * will return their names and that reflection metadata is registered for native images.
 *
 * @author Sam Brannen
 * @author Brian Clozel
 * @see org.springframework.nativex.substitutions.framework.test.Target_AbstractTestContextBootstrapper
 * @see org.springframework.test.context.support.AbstractTestContextBootstrapper.instantiateListeners
 */
class TestExecutionListenerFactoriesCodeContributor implements FactoriesCodeContributor {

	@Override
	public boolean canContribute(SpringFactory factory) {
		return factory.getFactoryType().getClassName().equals("org.springframework.test.context.TestExecutionListener");
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getClassName());
		generateReflectionMetadata(factory, context);
		code.writeToStaticBlock(builder -> {
			builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
					factory.getFactory().getClassName());
		});
	}

	private void generateReflectionMetadata(SpringFactory factory, BuildContext context) {
		String className = factory.getFactory().getClassName();
		ClassDescriptor classDescriptor = ClassDescriptor.of(className);
		classDescriptor.addMethodDescriptor(MethodDescriptor.of(MethodDescriptor.CONSTRUCTOR_NAME, (String[]) null));
		context.describeReflection(reflect -> reflect.add(classDescriptor));
	}

}
