package org.springframework.cloud.config.server;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CoreConfig;

import org.springframework.cloud.config.server.config.ConfigServerAutoConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ConfigServerAutoConfiguration.class, options = { "--enable-all-security-services" }, types = {
		@TypeHint(types = {MergeCommand.FastForwardMode.Merge.class, JGitText.class, CoreConfig.class,
				CoreConfig.AutoCRLF.class, CoreConfig.CheckStat.class, CoreConfig.EOL.class,
				CoreConfig.HideDotFiles.class, CoreConfig.SymLinks.class}, access = AccessBits.ALL)
})
public class ConfigServerHints implements NativeConfiguration {
}
