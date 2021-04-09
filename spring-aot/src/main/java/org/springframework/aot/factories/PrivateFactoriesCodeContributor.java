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
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.BuildContext;
import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.MethodDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.hint.Flag;
import org.springframework.util.StringUtils;

/**
 * {@link FactoriesCodeContributor} that handles default constructors which are not public.
 *
 * @author Brian Clozel
 */
class PrivateFactoriesCodeContributor implements FactoriesCodeContributor {

	private final Log logger = LogFactory.getLog(PrivateFactoriesCodeContributor.class);

	@Override
	public boolean canContribute(SpringFactory springFactory) {
		ClassDescriptor factory = springFactory.getFactory();
		return !factory.isPublic() || !factory.getDefaultConstructor().map(MethodDescriptor::isPublic).orElse(false);
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		TypeSystem typeSystem = context.getTypeSystem();
		boolean factoryOK = 
				passesConditionalOnClass(typeSystem, factory) && passesFilterCheck(typeSystem, factory) ;
		if (factoryOK) {
			String packageName = factory.getFactory().getPackageName();
			ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getCanonicalClassName());
			ClassName factoryClass = ClassName.bestGuess(factory.getFactory().getCanonicalClassName());
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
			if (factory.getFactoryType().getClassName().endsWith("EnvironmentPostProcessor")) {
				generateReflectionMetadata(factory.getFactory().getClassName(), context);
			}
		}
	}

	private boolean passesFilterCheck(TypeSystem typeSystem, SpringFactory factory) {
		String factoryName = factory.getFactory().getClassName();
		// TODO shame no ConditionalOnClass on these providers
		if (factoryName.equals("org.springframework.boot.diagnostics.analyzer.ValidationExceptionFailureAnalyzer")) {
			return typeSystem.resolveClass("javax.validation.ValidationException") != null;
		} else if (factoryName.equals("org.springframework.boot.liquibase.LiquibaseChangelogMissingFailureAnalyzer")) {
			return typeSystem.resolveClass("liquibase.exception.ChangeLogParseException") != null;
		} else if (factoryName.equals("org.springframework.boot.autoconfigure.jdbc.HikariDriverConfigurationFailureAnalyzer")) {
			return typeSystem.resolveClass("org.springframework.jdbc.CannotGetJdbcConnectionException") != null;
		}
		return true;
	}

	private String generateMethodName(ClassDescriptor factory) {
		return StringUtils.uncapitalize(factory.getShortName().replaceAll("\\.", ""));
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		org.springframework.nativex.domain.reflect.ClassDescriptor factoryDescriptor = org.springframework.nativex.domain.reflect.ClassDescriptor.of(factoryClassName);
		factoryDescriptor.setFlag(Flag.allDeclaredConstructors);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

}
