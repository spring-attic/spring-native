/*
 * Copyright 2019-2022 the original author or authors.
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

package org.springframework.aot.beans.factory.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * A No-Op scope used to replace a scope that is not supported in the native image.
 *
 * @author Stephane Nicoll
 */
public final class NoOpScope implements Scope {

	private final Object lock = new Object();

	private final Map<String, Object> map = new HashMap<>();

	private final List<Runnable> callbacks = new ArrayList<>();

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Object target = this.map.get(name);
		if (target == null) {
			synchronized (this.lock) {
				target = this.map.get(name);
				if (target == null) {
					target = objectFactory.getObject();
					this.map.put(name, target);
				}
			}
		}
		return target;
	}

	@Override
	public Object remove(String name) {
		synchronized (this.lock) {
			return this.map.remove(name);
		}
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		this.callbacks.add(callback);
	}

	public void close() {
		this.callbacks.forEach(Runnable::run);
	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

	@Override
	public String getConversationId() {
		return null;
	}

}
