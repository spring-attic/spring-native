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

import java.util.List;

import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;

/**
 * Allows plugins to the native image build process to participate in programmatic analysis of the application
 * and call back into the system to register proxies/reflective-access/etc.
 * 
 * @author Andy Clement
 */
public interface NativeImageContext {

	boolean addProxy(List<String> interfaces);

	boolean addProxy(String... interfaces);

	TypeSystem getTypeSystem();

	void addReflectiveAccess(String key, Flag... flags);

	void addReflectiveAccessHierarchy(Type type, Flag... flags);

	boolean hasReflectionConfigFor(String key);
}
