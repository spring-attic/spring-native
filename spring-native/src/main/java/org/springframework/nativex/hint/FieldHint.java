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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Configure reflection for a given field.
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration">Manual configuration of reflection use in native images</a>
 * @author Andy Clement
 * @author Sebastien Deleuze
 * @author Christoph Strobl
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldHint {

	/**
	 * The name of the field.
	 * @return the name
	 */
	String name();

	/**
	 * Allow unsafe access on the related field.
	 * @return {@code true} if allowed
	 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/#unsafe-accesses">Unsafe accesses</a>
	 */
	boolean allowUnsafeAccess() default false;

	/**
	 * Allow write access on the related field.
	 * @return {@code true} if allowed.
	 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration">Manual Configuration</a>
	 */
	boolean allowWrite() default false;

}
