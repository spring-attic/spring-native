package org.springframework.nativex.buildtools.factories;

import java.util.List;
import java.util.Optional;

import com.squareup.javapoet.ClassName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

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
		return AUTO_CONFIGURATION_TYPE.equals(factory.getFactoryType().getDottedName());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		TypeSystem typeSystem = factory.getFactory().getTypeSystem();

		// Condition checks
		// TODO make into a pluggable system
		boolean factoryOK =
			passesAnyConditionalOnClass(typeSystem, factory) &&
			passesAnyConditionalOnSingleCandidate(typeSystem, factory) &&
			passesAnyConditionalOnMissingBean(typeSystem, factory) &&
			passesIgnoreJmxConstraint(typeSystem, factory) &&
			passesAnyConditionalOnWebApplication(typeSystem, factory);

//		Optional<String> missingConditionClass = conditionClasses.stream()
//				.filter(conditionClass -> typeSystem.Lresolve(conditionClass, true) == null)
//				.findAny();
//		if (!missingConditionClass.isPresent()) {
		if (factoryOK) {
			ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getDottedName());
			code.writeToStaticBlock(builder -> {
				builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
						factory.getFactory().getDottedName());
			});
		}
	}
	
	private boolean passesIgnoreJmxConstraint(TypeSystem typeSystem, SpringFactory factory) {
		String name = factory.getFactory().getDottedName();
		if (name.toLowerCase().contains("jmx")) {
			return false;
		}
		return true;
	}
	
//	@Override
//	public boolean passesAnyConditionalOnClass(TypeSystem typeSystem, SpringFactory factory) {
//		List<String> conditionClasses = factory.getFactory().findConditionalOnClassValue();
//		Optional<String> missingConditionClass = conditionClasses.stream()
//				.filter(conditionClass -> typeSystem.Lresolve(conditionClass, true) == null)
//				.findAny();
//		if (missingConditionClass.isPresent()) {
//			return false;
//		}
//		return true;
//	}

	private boolean passesAnyConditionalOnSingleCandidate(TypeSystem typeSystem, SpringFactory factory) {
		String candidate = factory.getFactory().findConditionalOnSingleCandidateValue();
		return check(typeSystem, candidate);
	}

	private boolean passesAnyConditionalOnWebApplication(TypeSystem typeSystem, SpringFactory factory) {
		boolean b = factory.getFactory().checkConditionalOnWebApplication();
		return b;
	}

	private boolean passesAnyConditionalOnMissingBean(TypeSystem typeSystem, SpringFactory factory) {
		List<String> conditionClasses = factory.getFactory().findConditionalOnMissingBeanValue();
		// TODO if there are multiple is it really a fail if at least one is missing?
		Optional<String> missingConditionClass = conditionClasses.stream()
				.filter(conditionClass -> typeSystem.Lresolve(conditionClass, true) == null)
				.findAny();
		if (missingConditionClass.isPresent()) {
			return false;
		}
		return true;
	}
	
	private boolean check(TypeSystem typeSystem, String candidate) {
		if (candidate == null) {
			return true; // nothing to check, so its fine
		}
		Type resolvedType = typeSystem.Lresolve(candidate, true);
		return resolvedType!=null;
	}

}
