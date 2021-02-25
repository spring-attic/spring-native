package org.springframework.cloud.bootstrap;

import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.bootstrap.encrypt.EncryptionBootstrapConfiguration;
import org.springframework.cloud.bootstrap.encrypt.EnvironmentDecryptApplicationInitializer;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = RefreshAutoConfiguration.class, types = {
		@TypeHint(types = {BootstrapImportSelectorConfiguration.class, EnvironmentDecryptApplicationInitializer.class}, access = AccessBits.ALL)}, follow = true)
@NativeHint(trigger = BootstrapImportSelector.class, types =
		{@TypeHint(types = {PropertySourceBootstrapConfiguration.class, PropertySourceLocator.class, EncryptionBootstrapConfiguration.class})}, follow = true)
public class BootstrapHints implements NativeConfiguration {
}
