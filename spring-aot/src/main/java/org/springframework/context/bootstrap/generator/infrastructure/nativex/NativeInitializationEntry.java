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
package org.springframework.context.bootstrap.generator.infrastructure.nativex;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.util.Assert;

/**
 * Describe the need for initialization configuration.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/ClassInitialization/">GraalVM native image initialization documentation</a>
 */
public class NativeInitializationEntry {

	private final Kind kind;

	private final Class<?> type;

	private final String packageName;

	private NativeInitializationEntry(Kind kind, Class<?> type) {
		this.kind = kind;
		this.type = type;
		this.packageName = null;
	}

	private NativeInitializationEntry(Kind kind, String packageName) {
		this.kind = kind;
		this.type = null;
		this.packageName = packageName;
	}

	/**
	 * Create a new runtime {@link NativeInitializationEntry} for the specified type.
	 * @param type the related type
	 * @return the initialization entry
	 */
	public static NativeInitializationEntry ofRuntimeType(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		return new NativeInitializationEntry(Kind.RUNTIME, type);
	}

	/**
	 * Create a new build-time {@link NativeInitializationEntry} for the specified type.
	 * @param type the related type
	 * @return an initialization entry
	 */
	public static NativeInitializationEntry ofBuildTimeType(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		return new NativeInitializationEntry(Kind.BUILD_TIME, type);
	}

	/**
	 * Create a new runtime {@link NativeInitializationEntry} for the specified package name.
	 * @param packageName the related package name
	 * @return the initialization entry
	 */
	public static NativeInitializationEntry ofRuntimePackage(String packageName) {
		Assert.notNull(packageName, "packageName must not be null");
		return new NativeInitializationEntry(Kind.RUNTIME, packageName);
	}

	/**
	 * Create a new build-time {@link NativeInitializationEntry} for the specified package name.
	 * @param packageName the related package name
	 * @return the initialization entry
	 */
	public static NativeInitializationEntry ofBuildTimePackage(String packageName) {
		Assert.notNull(packageName, "packageName must not be null");
		return new NativeInitializationEntry(Kind.BUILD_TIME, packageName);
	}

	public void contribute(InitializationDescriptor descriptor) {
		if (this.kind == Kind.BUILD_TIME) {
			if (this.type != null) {
				descriptor.addBuildtimeClass(this.type.getName());
			}
			else {
				descriptor.addBuildtimePackage(this.packageName);
			}
		}
		else {
			if (this.type != null) {
				descriptor.addRuntimeClass(this.type.getName());
			}
			else {
				descriptor.addRuntimePackage(this.packageName);
			}
		}
	}

	enum Kind {

		/** Build-time initialization (use it carefully since it can introduce complex compatibility issues due to its viral nature) */
		BUILD_TIME,

		/** Runtime initialization (default, similar to the JVM) */
		RUNTIME,
	}


}
