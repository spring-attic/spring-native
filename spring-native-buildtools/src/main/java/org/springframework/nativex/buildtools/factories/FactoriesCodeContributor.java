package org.springframework.nativex.buildtools.factories;

import java.util.List;
import java.util.Optional;

import org.springframework.nativex.type.TypeSystem;

/**
 * Contribute code for instantiating Spring Factories.
 *
 * @author Brian Clozel
 */
interface FactoriesCodeContributor {

	/**
	 * Whether this contributor can contribute code for instantiating the given factory.
	 */
	boolean canContribute(SpringFactory factory);

	/**
	 * Contribute code for instantiating the factory given as argument.
	 */
	void contribute(SpringFactory factory, CodeGenerator code);

	default boolean passesAnyConditionalOnClass(TypeSystem typeSystem, SpringFactory factory) {
		List<String> conditionClasses = factory.getFactory().findConditionalOnClassValue();
		Optional<String> missingConditionClass = conditionClasses.stream()
				.filter(conditionClass -> typeSystem.Lresolve(conditionClass, true) == null)
				.findAny();
		if (missingConditionClass.isPresent()) {
			return false;
		}
		return true;
	}
}
