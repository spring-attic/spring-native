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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used by {@link NativeImageHint} annotations to indicate which classes/packages
 * should be initialized explicitly at buildtime or runtime.
 * 
 * @author Andy Clement
 */
@Repeatable(InitializationInfos.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializationInfo {
	
	Class<?>[] types() default {};
	
	String[] typeNames() default {};
	
	String[] packageNames() default {};
	
	InitializationTime initTime();

}