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
 * Used by {@link NativeHint} annotations to indicate which classes/packages
 * should be initialized explicitly at build-time or runtime.
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
@Repeatable(InitializationHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializationHint {

	/**
	 * Preferred way to configure initialization.
	 * @return the types
	 */
	Class<?>[] types() default {};

	/**
	 * Alternative way to configure initialization, should be used when type visibility prevents using {@link Class}
	 * references, or for nested types which should be specified using a {@code $} separator (for example
	 * {@code com.example.Foo$Bar}).
	 * @return the type names
	 */
	String[] typeNames() default {};

	/**
	 * Configure initialization for a set of packages.
	 * @return the package names
	 */
	String[] packageNames() default {};

	/**
	 * Set the initialization time, usually set to {@link InitializationTime#BUILD} since runtime is GraalVM native image default.
	 * @return the initialization time
	 */
	InitializationTime initTime();
}
