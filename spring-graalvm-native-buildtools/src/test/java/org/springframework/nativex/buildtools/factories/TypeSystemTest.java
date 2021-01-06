package org.springframework.nativex.buildtools.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.nativex.buildtools.TypeSystemExtension;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Brian Clozel
 */
@ExtendWith(TypeSystemExtension.class)
public class TypeSystemTest {

	@Test
	void checkThatEnableAutoConfigurationIsInClaspath(TypeSystem typeSystem) {
		Type type = typeSystem.resolveDotted("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
		assertThat(type).isNotNull();
	}
}
