package org.springframework.aot.thirdpartyhints;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.util.ClassUtils;

/**
 * @author Moritz Halbritter
 */
// TODO: Contribute these hints to graalvm-reachability-metadata repository
public class HibernateValidatorRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (!ClassUtils.isPresent("org.hibernate.validator.HibernateValidator", classLoader)) {
			return;
		}
		hints.reflection().registerType(TypeReference.of("org.hibernate.validator.internal.util.logging.Log_$logger"),
				hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerType(
				TypeReference.of("org.hibernate.validator.internal.util.logging.Messages_$bundle"),
				hint -> hint.withField("INSTANCE", fieldHint -> {
				}));
	}

}
