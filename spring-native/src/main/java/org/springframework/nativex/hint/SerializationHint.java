/*
 * Copyright 2021 the original author or authors.
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
 * Used by {@link NativeHint} annotations to indicate which types should be serializable in a native image.
 * Class references via the {@link #types} member are the preferred form of use but sometimes due
 * to accessibility restrictions or nested types the type names may need to be specified in the {@link #typeNames}
 * member.
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
@Repeatable(SerializationHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializationHint {

	/**
	 * Preferred way to configure types for serialization.
	 * @return the types
	 */
	Class<?>[] types() default {};

	/**
	 * Alternative way to configure serialization if visibility prevents {@link Class} references, 
	 * or for nested types which should be specific using a {@code $} separator
	 * (for example {@code com.example.Foo$Bar}).
	 * @return the type names
	 */
	String[] typeNames() default {};

	/**
	 * configure lambdaCapturingTypes for Lambda functions serialization
	 * @return the lambda capturing types
	 */
	Class<?>[] lambdaCapturingTypes() default {};

	/**
	 * Alternative way to configure lambdaCapturingTypes for Lambda functions serialization
	 * @return the lambda capturing type names
	 */
	String[] lambdaCapturingTypeNames() default {};

}
