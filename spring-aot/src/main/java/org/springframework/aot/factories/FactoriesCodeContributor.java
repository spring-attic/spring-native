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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.aot.BuildContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.TypeSystem;

/**
 * Contribute code for instantiating Spring Factories.
 *
 * @author Brian Clozel
 */
interface FactoriesCodeContributor {

	String CONDITIONAL_ON_CLASS = "org.springframework.boot.autoconfigure.condition.ConditionalOnClass";

	String CONDITIONAL_ON_WEBAPP = "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication";

	/**
	 * Whether this contributor can contribute code for instantiating the given factory.
	 */
	boolean canContribute(SpringFactory factory);

	/**
	 * Contribute code for instantiating the factory given as argument.
	 */
	void contribute(SpringFactory factory, CodeGenerator code, BuildContext context);

	default boolean passesConditionalOnClass(TypeSystem typeSystem, SpringFactory factory) {
		MergedAnnotation<Annotation> onClassCondition = factory.getFactory().getAnnotations().get(CONDITIONAL_ON_CLASS);
		if (onClassCondition.isPresent()) {
			AnnotationAttributes classConditions = onClassCondition
					.asAnnotationAttributes(MergedAnnotation.Adapt.CLASS_TO_STRING);
			Optional<String> missingClassValue = Arrays.stream(classConditions.getStringArray("value"))
					.filter(classCondition -> typeSystem.resolveClass(classCondition) == null).findAny();
			Optional<String> missingClassName = Arrays.stream(classConditions.getStringArray("name"))
					.filter(classCondition -> typeSystem.resolveClass(classCondition) == null).findAny();
			return !missingClassValue.isPresent() && !missingClassName.isPresent();
		}
		return true;
	}

	default boolean passesConditionalOnWebApplication(TypeSystem typeSystem, SpringFactory factory) {
		MergedAnnotation<Annotation> conditionalOnWebApp = factory.getFactory().getAnnotations().get(CONDITIONAL_ON_WEBAPP);
		if (conditionalOnWebApp.isPresent()) {
			Enum<?> webApplicationType = conditionalOnWebApp.asAnnotationAttributes().getEnum("type");
			if (webApplicationType.name().equals("SERVLET")) {
				return typeSystem.resolveClass("org.springframework.web.context.support.GenericWebApplicationContext") != null;
			}
			else if (webApplicationType.name().equals("REACTIVE")) {
				return typeSystem.resolveClass("org.springframework.web.reactive.HandlerResult") != null;
			}
			else { // ANY
				return (typeSystem.resolveClass("org.springframework.web.context.support.GenericWebApplicationContext") != null)
						|| (typeSystem.resolveClass("org.springframework.web.reactive.HandlerResult") != null);
			}
		}
		return true;
	}

}
