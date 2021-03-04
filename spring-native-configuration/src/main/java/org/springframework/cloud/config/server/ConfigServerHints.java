package org.springframework.cloud.config.server;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CoreConfig;

import org.springframework.boot.origin.OriginLookup;
import org.springframework.boot.origin.TextResourceOrigin;
import org.springframework.cloud.config.environment.PropertyValueDescriptor;
import org.springframework.cloud.config.server.config.ConfigServerAutoConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ConfigServerAutoConfiguration.class, options = { "--enable-all-security-services" }, types = {
		@TypeHint(types = {MergeCommand.FastForwardMode.Merge.class, JGitText.class, CoreConfig.class,
				CoreConfig.AutoCRLF.class, CoreConfig.CheckStat.class, CoreConfig.EOL.class,
				CoreConfig.HideDotFiles.class, CoreConfig.SymLinks.class, PropertyValueDescriptor.class,
				TextResourceOrigin.class, OriginLookup.class}, access = AccessBits.ALL)
})
public class ConfigServerHints implements NativeConfiguration {
}
