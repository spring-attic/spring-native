package org.springframework.boot.autoconfigure.rsocket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

// TODO Should be possible to guess that automatically
@NativeImageHint(trigger = RSocketMessagingAutoConfiguration.class, typeInfos= {
		@TypeInfo(types = {Mono.class, Flux.class}, access = AccessBits.CLASS)
})
public class RSocketMessagingHints implements NativeImageConfiguration {
}
