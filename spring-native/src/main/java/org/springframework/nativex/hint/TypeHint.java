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
 * Configure reflection or {@code *.class} resources access on specified types. Class references
 * via the {@link #types()} member are the preferred form of use but sometimes due to accessibility restrictions the type names
 * may need to be specified in the {@link #typeNames} member. See {@link AccessBits} for the configurable types of access - it
 * can be important to limit it to only what is necessary for an absolutely optimal compiled image size.
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration">Manual configuration of reflection use in native images</a>
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
@Repeatable(TypeHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeHint {

	/**
	 * Preferred (because typesafe) way to specify class references.
	 * @return the types
	 */
	Class<?>[] types() default {};

	/**
	 * Alternative way to specify class references, should be used when type visibility prevents using {@link Class}
	 * references, or for nested types which should be specified using a {@code $} separator (for example
	 * {@code com.example.Foo$Bar}).
	 * @return the type names
	 */
	String[] typeNames() default {};

	/**
	 * Access scope to be configured (class and declared constructors by default). See the various predefined ones
	 * defined in {@link AccessBits}. You can also use custom combinations like {@code CLASS | DECLARED_FIELDS}.
	 * @return the access
	 */
	int access() default AccessBits.LOAD_AND_CONSTRUCT;

	/**
	 * Specific method information, useful to reduce the footprint impact of the generated configuration.
	 * @return the methods information
	 */
	MethodHint[] methods() default {};

	/**
	 * Specific fields information, useful to reduce the footprint impact of the generated configuration or to specify
	 * unsafe access.
	 * @return the fields information
	 */
	FieldHint[] fields() default {};
}
