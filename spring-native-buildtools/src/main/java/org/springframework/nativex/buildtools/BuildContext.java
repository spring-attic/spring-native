/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.nativex.buildtools;

import java.util.function.Consumer;

import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.type.TypeSystem;

/**
 * Provide build context information.
 *
 * @author Brian Clozel
 */
public interface BuildContext {

	/**
	 * Return the "compile+runtime" {@link ClassLoader} for the application.
	 * @deprecated in favor of the {@link #getTypeSystem() TypeSystem}.
	 */
	@Deprecated
	ClassLoader getClassLoader();

	/**
	 * Return the {@link TypeSystem} based on the "compile+runtime" application classpath.
	 */
	TypeSystem getTypeSystem();

	/**
	 * Contribute source files to the application.
	 * <p>This additional source code will be compiled and packaged with the application by a build plugin.
	 */
	void addSourceFiles(SourceFile... sourceFile);

	/**
	 * Contribute resource files to the application.
	 * <p>This additional packaged with the application by a build plugin.
	 */
	void addResources(ResourceFile... resourceFile);

	/**
	 * Contribute reflection information to the application.
	 */
	void describeReflection(Consumer<ReflectionDescriptor> consumer);

	/**
	 * Contribute proxies information to the application.
	 */
	void describeProxies(Consumer<ProxiesDescriptor> consumer);

	/**
	 * Contribute resources information to the application.
	 */
	void describeResources(Consumer<ResourcesDescriptor> consumer);

}
