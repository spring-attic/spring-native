package org.yaml.snakeyaml;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;

public class SnakeYamlHints implements NativeConfiguration {

	@Override
	public boolean isValid(AotOptions aotOptions) {
		return !aotOptions.isRemoveYamlSupport();
	}

	@Override
	public void computeHints(NativeConfigurationRegistry registry, AotOptions aotOptions) {
		registry.reflection().forType(Yaml.class).withAccess(TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS);
	}
}
