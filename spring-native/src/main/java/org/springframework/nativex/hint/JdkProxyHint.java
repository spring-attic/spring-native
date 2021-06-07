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
 * Used by {@link NativeHint} annotations to indicate which interfaces (since only JDK dynamic proxies are supported)
 * sets of types need proxies.
 * Interface references via the {@link #types} member are the preferred form of use but sometimes due
 * to accessibility restrictions or nested types the type names may need to be specified in the {@link #typeNames}
 * member.  The ordering of types when building a proxy is important, therefore we don't allow mixing the class reference
 * and string styles as it may not result in the ordering you entered the attributes in at the source level. In these cases
 * it is best to simple use the typeNames attribute (the ordering within a particular attribute is guaranteed).
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/DynamicProxy/">Dynamic proxy in native image</a>
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
@Repeatable(JdkProxyHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface JdkProxyHint {

	/**
	 * Preferred way to configure interfaces for a given JDK dynamic proxy.
	 * @return the types
	 */
	Class<?>[] types() default {};

	/**
	 * Alternative way to configure interfaces for a given JDK dynamic proxy, should be used when type visibility
	 * prevents using {@link Class} references, or for nested types which should be specific using a {@code $} separator
	 * (for example {@code com.example.Foo$Bar}).
	 * @return the type names
	 */
	String[] typeNames() default {};

}
