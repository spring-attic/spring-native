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
import java.util.List;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.aot.BuildContext;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeUtils;

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
	
	/**
	 * It is possible to ask for property checks to be done at build time - this enables chunks of code to be discarded early
	 * and not included in the image. 
	 * 
	 * The format is of this style (which you specify with the call to mvn):
	 * <ul>
	 * <li> {@code -Dspring.native.build-time-properties-checks=} switches on build time evaluation of some configuration
	 * conditions related to properties. It must include at least an initial setting of {@code default-include-all} or
	 * {@code default-exclude-all} and that may be followed by a comma separated list of prefixes to explicitly include
	 * or exclude (for example {@code =default-include-all,!spring.dont.include.these.,!or.these} or
	 * {@code =default-exclude-all,spring.include.this.one.though.,and.this.one}).
	 * When considering a property the longest matching prefix in this setting will apply (in cases where a property matches 
	 * multiple prefixes).
	 * 
	 * <li> {@code -Dspring.native.build-time-properties-match-if-missing=false} means for any properties specifying
	 * {@code matchIfMissing=true} that will be overridden and not respected. This does flip the application into a
	 * mode where it needs to be much more explicit about specifying properties that activate configurations. 
	 * </ul>
	 * 
	 * @return true if checks pass, false if one fails and the type should be considered inactive, in which case failedPropertyChecks
	 * includes information on what check failed
	 */
	default boolean passesAnyPropertyRelatedConditions(List<String> classpath, TypeSystem typeSystem, SpringFactory factory, List<String> failedPropertyChecks, AotOptions aotOptions) {
		ClassDescriptor resolvedFactory = factory.getFactory();
		String factoryName = resolvedFactory.getClassName();
		// Problems observed discarding inner configurations due to eager property checks
		// (configserver sample). Too aggressive, hence the $ check
		if (aotOptions.isBuildTimePropertyChecking() && !factoryName.contains("$")) {
			org.springframework.nativex.type.TypeSystem legacyTypeSystem = new org.springframework.nativex.type.TypeSystem(classpath);
			Type legacyResolvedFactory = legacyTypeSystem.resolve(resolvedFactory);
			String testResult = TypeUtils.testAnyConditionalOnProperty(legacyResolvedFactory, aotOptions);
			if (testResult != null) {
				String message = factoryName+" FAILED ConditionalOnProperty property check: "+testResult;
				failedPropertyChecks.add(message);
				return false;
			}
			// These are like a ConditionalOnProperty check but using a special condition to check the property
			testResult = TypeUtils.testAnyConditionalOnAvailableEndpoint(legacyResolvedFactory, aotOptions);
			if (testResult != null) {
				String message = factoryName+" FAILED ConditionalOnAvailableEndpoint property check: "+testResult;
				failedPropertyChecks.add(message);
				return false;
			}
			testResult = TypeUtils.testAnyConditionalOnEnabledMetricsExport(legacyResolvedFactory, aotOptions);
			if (testResult != null) {
				String message = factoryName+" FAILED ConditionalOnEnabledMetricsExport property check: "+testResult;
				failedPropertyChecks.add(message);
				return false;
			}
			testResult = TypeUtils.testAnyConditionalOnEnabledHealthIndicator(legacyResolvedFactory, aotOptions);
			if (testResult != null) {
				String message = factoryName+" FAILED ConditionalOnEnabledHealthIndicator property check: "+testResult;
				failedPropertyChecks.add(message);
				return false;
			}
		}
		return true;
	}
}
