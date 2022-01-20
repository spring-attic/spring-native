/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.boot.context.properties;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;

import org.springframework.aot.context.bootstrap.generator.BeanDefinitionExcludeFilter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriterSupplier;
import org.springframework.aot.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.ProtectedAccessAnalysis;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

/**
 *
 * @author Stephane Nicoll
 */
@Order(0)
class ConfigurationPropertiesBinderBeanRegistrationWriterSupplier implements BeanRegistrationWriterSupplier, BeanDefinitionExcludeFilter {

	static final String BEAN_NAME = "org.springframework.boot.context.internalConfigurationPropertiesBinder";

	static final String FACTORY_BEAN_NAME = "org.springframework.boot.context.internalConfigurationPropertiesBinderFactory";

	@Override
	public BeanRegistrationWriter get(String beanName, BeanDefinition beanDefinition) {
		return (BEAN_NAME.equals(beanName)) ? createBeanRegistrationWriter(beanName, beanDefinition) : null;
	}

	@Override
	public boolean isExcluded(String beanName, BeanDefinition beanDefinition) {
		return FACTORY_BEAN_NAME.equals(beanName);
	}

	private BeanRegistrationWriter createBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition) {
		BeanInstanceDescriptor descriptor = BeanInstanceDescriptor.of(ConfigurationPropertiesBinder.class)
				.withInstanceCreator(ReflectionUtils.findMethod(ConfigurationPropertiesBinder.class, "register"))
				.build();
		return new DefaultBeanRegistrationWriter(beanName, beanDefinition, descriptor) {
			@Override
			public void writeBeanRegistration(BootstrapWriterContext context, Builder code) {
				ProtectedAccessAnalysis analysis = context.getProtectedAccessAnalyzer().analyze(descriptor);
				if (analysis.isAccessible()) {
					code.addStatement(writeRegistration());
				}
				else {
					String protectedPackageName = analysis.getPrivilegedPackageName();
					BootstrapClass javaFile = context.getBootstrapClass(protectedPackageName);
					MethodSpec method = javaFile.addMethod(MethodSpec.methodBuilder("registerConfigurationPropertiesBinder")
							.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
							.addParameter(DefaultListableBeanFactory.class, "beanFactory")
							.addStatement(writeRegistration()));
					code.addStatement("$T.$N(beanFactory)", javaFile.getClassName(), method);
				}
			}

			private CodeBlock writeRegistration() {
				return CodeBlock.of("$T.register(beanFactory)", ConfigurationPropertiesBinder.class);
			}

		};
	}

}
