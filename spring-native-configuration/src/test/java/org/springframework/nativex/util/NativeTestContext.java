/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.nativex.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * A {@link NativeContext} designed to capture added configurations in memory allowing verification within tests.
 *
 * @author Christoph Strobl
 */
public class NativeTestContext implements NativeContext {

	private final TypeSystem typeSystem;

	MultiValueMap<String, List<String>> proxies;
	MultiValueMap<String, List<Flag>> reflection;
	Set<Type> builtTimeInit;
	Set<String> resources;

	public NativeTestContext() {
		this(new TestTypeSystem());
	}

	public NativeTestContext(TypeSystem typeSystem) {

		this.typeSystem = typeSystem;

		this.proxies = new LinkedMultiValueMap<>();
		this.reflection = new LinkedMultiValueMap<>();
		this.builtTimeInit = new LinkedHashSet<>();
		this.resources = new LinkedHashSet<>();
	}

	@Override
	public boolean addProxy(List<String> interfaces) {

		proxies.add(interfaces.get(0), interfaces);
		return true;
	}

	@Override
	public boolean addProxy(String... interfaces) {
		return addProxy(Arrays.asList(interfaces));
	}

	@Override
	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	@Override
	public void addReflectiveAccess(String key, Flag... flags) {
		reflection.add(key, Arrays.asList(flags));
	}

	@Override
	public Set<String> addReflectiveAccessHierarchy(String typename, int accessBits) {

		Type type = typeSystem.resolveDotted(typename, true);
		Set<String> added = new TreeSet<>();
		registerHierarchy(type, added, accessBits);
		return added;
	}

	private void registerHierarchy(Type type, Set<String> visited, int accessBits) {
		String typename = type.getDottedName();
		if (visited.add(typename)) {
			addReflectiveAccess(typename, AccessBits.getFlags(accessBits));
			Set<String> relatedTypes = type.getTypesInSignature();
			for (String relatedType : relatedTypes) {
				Type t = typeSystem.resolveSlashed(relatedType, true);
				if (t != null) {
					registerHierarchy(t, visited, accessBits);
				}
			}
		}
	}

	@Override
	public boolean hasReflectionConfigFor(String typename) {
		return reflection.containsKey(typename);
	}

	@Override
	public void initializeAtBuildTime(Type type) {
		builtTimeInit.add(type);
	}

	@Override
	public void log(String string) {

	}

	@Override
	public void addResourceBundle(String string) {
		resources.add(string);
	}
}
