package org.springframework.aot.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.aot.TypeSystemExtension;
import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.TypeSystem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
public class TypeSystemTest {

	@Test
	void checkThatEnableAutoConfigurationIsInClaspath(TypeSystem typeSystem) {
		ClassDescriptor resolved = typeSystem.resolveClass("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
		assertThat(resolved).isNotNull();
	}
}
