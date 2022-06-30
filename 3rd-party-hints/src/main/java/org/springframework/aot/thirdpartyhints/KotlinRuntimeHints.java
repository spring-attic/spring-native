package org.springframework.aot.thirdpartyhints;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.KotlinDetector;

/**
 * @author Sebastien Deleuze
 */
// TODO: Contribute these hints to graalvm-reachability-metadata repository
public class KotlinRuntimeHints implements RuntimeHintsRegistrar {


	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (!KotlinDetector.isKotlinReflectPresent()) {
			return;
		}
		hints.resources()
				.registerPattern("META-INF/*.kotlin_module")
				.registerPattern("*.kotlin_builtins");

		ReflectionHints reflectionHints = hints.reflection();

		reflectionHints.registerType(TypeReference.of("kotlin.reflect.full.KClasses"),
				builder -> {});
		reflectionHints.registerType(TypeReference.of("kotlin.Metadata"),
				builder ->builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS));
		reflectionHints.registerType(TypeReference.of("kotlin.reflect.jvm.internal.ReflectionFactoryImpl"),
				builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		reflectionHints.registerType(TypeReference.of("kotlin.reflect.jvm.internal.impl.resolve.scopes.DescriptorKindFilter"),
				builder -> builder.withMembers(MemberCategory.DECLARED_FIELDS));
	}
}
