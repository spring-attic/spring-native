package org.springframework.http.codec;

import java.util.Map;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.http.codec.support.DefaultClientCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;

@TargetClass(className = "org.springframework.http.codec.CodecConfigurerFactory", onlyWith = { OnlyIfPresent.class })
final class Target_CodecConfigurerFactory {

	@Delete
	private static String DEFAULT_CONFIGURERS_PATH;

	@Delete
	private static Map<Class<?>, Class<?>> defaultCodecConfigurers;

	@Substitute
	public static <T extends CodecConfigurer> T create(Class<T> ifc) {
		if (ifc == ClientCodecConfigurer.class) {
			return (T) new DefaultClientCodecConfigurer();
		}
		else if (ifc == ServerCodecConfigurer.class) {
			return (T) new DefaultServerCodecConfigurer();
		}
		throw new IllegalStateException("No default codec configurer found for " + ifc);
	}
}
