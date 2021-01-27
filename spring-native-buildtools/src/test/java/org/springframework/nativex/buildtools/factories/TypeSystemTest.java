package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.buildtools.TypeSystemExtension;

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
