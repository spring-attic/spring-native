/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graalvm.extension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.graalvm.type.AccessBits;

/**
 * Used by {@link NativeImageHint} annotations to indicate which types need which type of access. Class references 
 * via the `types()` member are the preferred form of use but sometimes due to accessibility restrictions the type names
 * may need to be specified in the `typeNames()` member. See {@link AccessBits} for the configurable types of access - it
 * can be important to limit it to only what is necessary for an absolutely optimal compiled image size.
 * 
 * @author Andy Clement
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeInfo {
	
	public Class<?>[] types() default {};

	public String[] typeNames() default {};

	public int access() default AccessBits.ALL;

	// TODO How to specify particular constructors/methods/fields? Does it matter?
	// public MemberInfo[] members() default {}; ?
}
