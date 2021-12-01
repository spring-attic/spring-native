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

import java.lang.reflect.Constructor;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.beans.BeanUtils;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.TypeAccess;

import com.squareup.javapoet.ClassName;

/**
 * {@link FactoriesCodeContributor} that contributes source code for some factories
 * that are missing a no-arg constructor and require injection of specific parameters.
 * <p>Instead of instantiating them statically, we're making sure that
 * {@link org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}
 * will return their name and we're also adding reflection metadata for native images.
 *
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
public class NoArgConstructorFactoriesCodeContributor implements FactoriesCodeContributor {

	@Override
	public boolean canContribute(SpringFactory factory) {
		Constructor<?> constructor = null;
		try {
			constructor = BeanUtils.getResolvableConstructor(factory.getFactory());
		} catch (IllegalStateException | NoClassDefFoundError ex) {
		}
		return constructor == null || constructor.getParameterCount() > 0;
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getName());
		generateReflectionMetadata(factory.getFactory().getName(), context);
		code.writeToStaticBlock(builder -> {
			builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
					factory.getFactory().getName());
		});
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		ClassDescriptor factoryDescriptor = ClassDescriptor.of(factoryClassName);
		factoryDescriptor.setAccess(TypeAccess.DECLARED_CONSTRUCTORS);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

}
