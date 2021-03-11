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

package org.springframework.core.type.classreading;

import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Describe the method information for a Java class.
 * @author Brian Clozel
 */
public interface MethodDescriptor extends AnnotatedTypeMetadata {

	String getMethodName();

	ClassDescriptor getDeclaringClass();

	TypeDescriptor getReturnType();

	boolean isAbstract();

	boolean isStatic();

	boolean isFinal();

	boolean isOverridable();

	boolean isConstructor();

	boolean isPublic();

	boolean isPrivate();

	int getArgumentsCount();

}
