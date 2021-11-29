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
import java.lang.reflect.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.beans.BeanUtils;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link FactoriesCodeContributor} that handles default constructors which are not public.
 *
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
class PrivateFactoriesCodeContributor implements FactoriesCodeContributor {

	private final Log logger = LogFactory.getLog(PrivateFactoriesCodeContributor.class);

	@Override
	public boolean canContribute(SpringFactory springFactory) {
		Class<?> factory = springFactory.getFactory();
		try {
			Constructor<?> constructor = BeanUtils.getResolvableConstructor(factory);
			return !Modifier.isPublic(factory.getModifiers()) ||
					(constructor != null && constructor.getParameterCount() == 0 && !Modifier.isPublic(constructor.getModifiers()));
		} catch (IllegalStateException | NoClassDefFoundError ex) {
			return false;
		}
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		boolean factoryOK = 
				passesConditionalOnClass(context, factory) && passesFilterCheck(context, factory) ;
		if (factoryOK) {
			String packageName = factory.getFactory().getPackageName();
			ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getCanonicalName());
			ClassName factoryClass = ClassName.bestGuess(factory.getFactory().getCanonicalName());
			ClassName staticFactoryClass = ClassName.get(packageName, code.getStaticFactoryClass(packageName).name);
			MethodSpec creator = MethodSpec.methodBuilder(generateMethodName(factory.getFactory()))
					.addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
					.returns(factoryClass)
					.addStatement("return new $T()", factoryClass).build();
			code.writeToStaticFactoryClass(packageName, builder -> builder.addMethod(creator));
			code.writeToStaticBlock(block -> {
				block.addStatement("factories.add($T.class, () -> $T.$N())", factoryTypeClass, staticFactoryClass, creator);
			});
			// TODO To be removed, currently required due to org.springframework.boot.env.ReflectionEnvironmentPostProcessorsFactory
			if (factory.getFactoryType().getName().endsWith("EnvironmentPostProcessor")) {
				generateReflectionMetadata(factory.getFactory().getName(), context);
			}
		}
	}

	private boolean passesFilterCheck(BuildContext context, SpringFactory factory) {
		String factoryName = factory.getFactory().getName();
		// TODO shame no ConditionalOnClass on these providers
		if (factoryName.equals("org.springframework.boot.diagnostics.analyzer.ValidationExceptionFailureAnalyzer")) {
			return ClassUtils.isPresent("javax.validation.ValidationException", context.getClassLoader());
		} else if (factoryName.equals("org.springframework.boot.liquibase.LiquibaseChangelogMissingFailureAnalyzer")) {
			return ClassUtils.isPresent("liquibase.exception.ChangeLogParseException", context.getClassLoader());
		} else if (factoryName.equals("org.springframework.boot.autoconfigure.jdbc.HikariDriverConfigurationFailureAnalyzer")) {
			return ClassUtils.isPresent("org.springframework.jdbc.CannotGetJdbcConnectionException", context.getClassLoader());
		}
		return true;
	}

	private String generateMethodName(Class<?> factory) {
		return StringUtils.uncapitalize(ClassUtils.getShortName(factory).replaceAll("\\.", ""));
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		org.springframework.nativex.domain.reflect.ClassDescriptor factoryDescriptor = org.springframework.nativex.domain.reflect.ClassDescriptor.of(factoryClassName);
		factoryDescriptor.setFlag(Flag.allDeclaredConstructors);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

}
