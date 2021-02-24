package org.springframework.cloud.bootstrap;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = BootstrapApplicationListener.class, types = {@TypeInfo(types = {BootstrapImportSelectorConfiguration.class}, access = AccessBits.ALL)})
public class BootstrapHints implements NativeConfiguration {
}
