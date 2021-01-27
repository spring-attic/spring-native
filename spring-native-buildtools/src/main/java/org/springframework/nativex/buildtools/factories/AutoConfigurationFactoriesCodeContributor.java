package org.springframework.nativex.buildtools.factories;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

import com.squareup.javapoet.ClassName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.buildtools.BuildContext;

/**
 * {@link FactoriesCodeContributor} that contributes source code for {@code EnableAutoConfiguration} factories.
 * <p>Instead of instantiating them statically, we're making sure that
 * {@link org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}
 * will return their name and we're also adding reflection metadata for native images.
 * <p>For optimization purposes, this contributor can also ignore auto-configurations with
 * conditional annotations that will not match at runtime.
 *
 * @author Brian Clozel
 */
public class AutoConfigurationFactoriesCodeContributor implements FactoriesCodeContributor {

	private static String AUTO_CONFIGURATION_TYPE = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

	private final Log logger = LogFactory.getLog(AutoConfigurationFactoriesCodeContributor.class);


	@Override
	public boolean canContribute(SpringFactory factory) {
		return AUTO_CONFIGURATION_TYPE.equals(factory.getFactoryType().getClassName());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		TypeSystem typeSystem = context.getTypeSystem();
		// Condition checks
		// TODO make into a pluggable system
		boolean factoryOK =
				passesAnyConditionalOnClass(typeSystem, factory) &&
						passesAnyConditionalOnSingleCandidate(typeSystem, factory) &&
						passesAnyConditionalOnMissingBean(typeSystem, factory) &&
						passesIgnoreJmxConstraint(typeSystem, factory) &&
						passesAnyConditionalOnWebApplication(typeSystem, factory);

		if (factoryOK) {
			ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getCanonicalClassName());
			code.writeToStaticBlock(builder -> {
				builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
						factory.getFactory().getCanonicalClassName());
			});
		}
	}

	private boolean passesIgnoreJmxConstraint(TypeSystem typeSystem, SpringFactory factory) {
		String name = factory.getFactory().getCanonicalClassName();
		if (name.toLowerCase().contains("jmx")) {
			return false;
		}
		return true;
	}

	private boolean passesAnyConditionalOnSingleCandidate(TypeSystem typeSystem, SpringFactory factory) {
		MergedAnnotation<Annotation> onSingleCandidate = factory.getFactory().getAnnotations()
				.get("org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate");
		if (onSingleCandidate.isPresent()) {
			String singleCandidateClass = onSingleCandidate.asAnnotationAttributes(MergedAnnotation.Adapt.CLASS_TO_STRING)
					.getString("value");
			return typeSystem.resolveClass(singleCandidateClass) != null;
		}
		return true;
	}

	private boolean passesAnyConditionalOnMissingBean(TypeSystem typeSystem, SpringFactory factory) {
		MergedAnnotation<Annotation> missingBeanCondition = factory.getFactory().getAnnotations()
				.get("org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean");
		if (missingBeanCondition.isPresent()) {
			AnnotationAttributes attributes = missingBeanCondition.asAnnotationAttributes(MergedAnnotation.Adapt.CLASS_TO_STRING);
			return Stream.concat(Arrays.stream(attributes.getStringArray("value")),
					Arrays.stream(attributes.getStringArray("type")))
					.anyMatch(beanClass -> typeSystem.resolveClass(beanClass) == null);
		}
		return true;
	}

}
