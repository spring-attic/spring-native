package org.springframework.boot.web.client;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = org.apache.http.client.HttpClient.class, types = @TypeHint(types = {
		org.apache.http.client.HttpClient.class,
		org.springframework.http.client.HttpComponentsClientHttpRequestFactory.class
}))
@NativeHint(trigger = okhttp3.OkHttpClient.class, types = @TypeHint(types = {
		okhttp3.OkHttpClient.class,
		org.springframework.http.client.OkHttp3ClientHttpRequestFactory.class
}))
public class ClientHttpRequestFactoryHints implements NativeConfiguration {
}
