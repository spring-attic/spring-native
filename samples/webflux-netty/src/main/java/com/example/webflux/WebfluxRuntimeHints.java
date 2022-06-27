package com.example.webflux;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.support.RuntimeHintsUtils;
import org.springframework.http.codec.support.DefaultClientCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Moritz Halbritter
 */
class WebfluxRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // TODO: See https://github.com/spring-projects/spring-framework/issues/28701
        hints.resources().registerPattern("org/springframework/http/codec/CodecConfigurer.properties");
        hints.reflection().registerType(DefaultClientCodecConfigurer.class,
                hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
        hints.reflection().registerType(DefaultServerCodecConfigurer.class,
                hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
        RuntimeHintsUtils.registerAnnotation(hints, RequestMapping.class);
    }
}
