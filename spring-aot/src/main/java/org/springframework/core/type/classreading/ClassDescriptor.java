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

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.Nullable;

/**
 * @author Brian Clozel
 */
public interface ClassDescriptor extends AnnotatedTypeMetadata {

	String getPackageName();

	String getClassName();

	String getCanonicalClassName();

	String getShortName();
	
	boolean isInterface();

	boolean isAnnotation();

	boolean isAbstract();

	default boolean isConcrete() {
		return !(isInterface() || isAbstract());
	}

	boolean isPublic();

	@Nullable
	ClassDescriptor getSuperClass();

	Stream<ClassDescriptor> getInterfaces();

	@Nullable
	ClassDescriptor getEnclosingClass();

	Stream<ClassDescriptor> getMemberClasses();

	Stream<MethodDescriptor> getMethods();

	default Stream<MethodDescriptor> getConstructors() {
		return getMethods().filter(MethodDescriptor::isConstructor);
	}

	default Optional<MethodDescriptor> getDefaultConstructor() {
		return getConstructors().filter(method -> method.getArgumentsCount() == 0).findAny();
	}

}
