package org.springframework.nativex.buildtools.factories;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.buildtools.BuildContext;

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

	default boolean passesAnyConditionalOnClass(TypeSystem typeSystem, SpringFactory factory) {
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

	default boolean passesAnyConditionalOnWebApplication(TypeSystem typeSystem, SpringFactory factory) {
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
