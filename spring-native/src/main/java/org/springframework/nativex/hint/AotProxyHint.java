/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.nativex.hint;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used by {@link NativeHint} annotations to indicate which classes need a proxy generating at build time - these
 * proxies enable runtime Spring features like AOP to function correctly.
 *
 * @author Andy Clement
 */
@Repeatable(AotProxyHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AotProxyHint {

	Class<?> targetClass() default Object.class;
	
	String targetClassName() default "java.lang.Object";
	
	/**
	 * Preferred way to configure interfaces for a class proxy.
	 * @return the types
	 */
	Class<?>[] interfaces() default {};
	
	/**
	 * Alternative way to configure interfaces for a class proxy, should be used when type visibility
	 * prevents using {@link Class} references, or for nested types which should be specific using a {@code $} separator
	 * (for example {@code com.example.Foo$Bar}).
	 * @return the type names
	 */
	String[] interfaceNames() default {};

	int proxyFeatures() default ProxyBits.NONE;

}
