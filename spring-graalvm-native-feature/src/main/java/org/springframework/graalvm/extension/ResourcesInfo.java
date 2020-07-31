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
 * Used by {@link NativeImageHint} annotations to indicate which resources should be pulled into the image. 
 * Resources are described by patterns and may be resource bundles (in which case <tt>isBundle</tt> should
 * be set).
 * 
 * @author Andy Clement
 */
@Repeatable(ResourcesInfos.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcesInfo {
	
	String[] patterns() default {};
	
	boolean isBundle() default false;

}
