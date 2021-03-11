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

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

/**
 * Default implementation for {@link TypeSystem}
 * @author Brian Clozel
 */
class DefaultTypeSystem implements TypeSystem {

	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	private final ResourcePatternResolver resourcePatternResolver;

	private final ResourceLoader resourceLoader;

	DefaultTypeSystem(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
	}

	@Override
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	@Override
	public TypeDescriptor resolve(String typeName) {
		try {
			return new DefaultTypeDescriptor(typeName, this);
		}
		catch (IOException exc) {
			throw new RuntimeException("Cannot resolve " + typeName, exc);
		}
	}

	@Override
	public Stream<ClassDescriptor> scan(String basePackage) {
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath(basePackage) + DEFAULT_RESOURCE_PATTERN;
			return Arrays.stream(this.resourcePatternResolver.getResources(packageSearchPath))
					.filter(Resource::exists)
					.map(resource -> {
						try {
							return DefaultClassDescriptorReader.readDescriptor(this, resource);
						}
						catch (IOException exception) {
							throw new RuntimeException("Cannot read type for: " + resource.getDescription(), exception);
						}
					});
		}
		catch (IOException exc) {
			throw new RuntimeException("Could not load resources in package: " + basePackage, exc);
		}
	}

}