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
 * Used by {@link NativeHint} annotations to indicate which resources should be pulled into the image.
 * Resources are described by patterns and may be resource bundles (in which case {@link #isBundle} should
 * be set).
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Resources/">Accessing resources in native images</a>
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
@Repeatable(ResourcesHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceHint {

	/**
	 * Resource patterns specified with Java regexp for regular resources, and with the bundle name (regexp not supported) if {@link #isBundle()} is set to true.
	 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Resources/">Accessing resources in native images</a>
	 * return the patterns
	 * @return the patterns of the resources to include
	 */
	String[] patterns() default {};

	/**
	 * Specify the {@link #patterns()} identify a bundle when set to true.
	 * @return {@code true} if a bundle, {@code false} otherwise.
	 */
	boolean isBundle() default false;

}
