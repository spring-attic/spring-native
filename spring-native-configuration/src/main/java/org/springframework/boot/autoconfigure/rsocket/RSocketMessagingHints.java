package org.springframework.boot.autoconfigure.rsocket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.hint.AccessBits;

// TODO Should be possible to guess that automatically
@NativeHint(trigger = RSocketMessagingAutoConfiguration.class, typeInfos= {
		@TypeInfo(types = {Mono.class, Flux.class}, access = AccessBits.CLASS)
})
public class RSocketMessagingHints implements NativeConfiguration {
}
