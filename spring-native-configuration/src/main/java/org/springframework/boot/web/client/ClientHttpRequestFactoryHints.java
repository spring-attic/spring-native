package org.springframework.boot.web.client;

import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = org.apache.http.client.HttpClient.class, types = {
		@TypeHint(types = org.apache.http.client.HttpClient.class),
		@TypeHint(types = org.springframework.http.client.HttpComponentsClientHttpRequestFactory.class, methods = {
				@MethodHint(name = "setConnectTimeout", parameterTypes = int.class),
				@MethodHint(name = "setReadTimeout", parameterTypes = int.class),
				@MethodHint(name = "setBufferRequestBody", parameterTypes = boolean.class),
		})
})
@NativeHint(trigger = okhttp3.OkHttpClient.class, types = {
		@TypeHint(types = okhttp3.OkHttpClient.class),
		@TypeHint(types = org.springframework.http.client.OkHttp3ClientHttpRequestFactory.class, methods = {
				@MethodHint(name = "setConnectTimeout", parameterTypes = int.class),
				@MethodHint(name = "setReadTimeout", parameterTypes = int.class),
				@MethodHint(name = "setBufferRequestBody", parameterTypes = boolean.class),
		})
})
@NativeHint(trigger = org.springframework.http.client.SimpleClientHttpRequestFactory.class, types =
		@TypeHint(types = org.springframework.http.client.SimpleClientHttpRequestFactory.class, methods = {
				@MethodHint(name = "setConnectTimeout", parameterTypes = int.class),
				@MethodHint(name = "setReadTimeout", parameterTypes = int.class),
				@MethodHint(name = "setBufferRequestBody", parameterTypes = boolean.class),
		})
)
public class ClientHttpRequestFactoryHints implements NativeConfiguration {
}
