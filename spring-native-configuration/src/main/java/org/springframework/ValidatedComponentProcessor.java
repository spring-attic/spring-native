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

package org.springframework;

import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recognize spring.components that need validate proxies and register them.
 *
 * @author Petr Hejl
 */
public class ValidatedComponentProcessor implements ComponentProcessor {

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		return type.isAtValidated(true);
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		boolean hasInterfaceMethods = Arrays.stream(type.getAllInterfaces()).anyMatch(type1 -> !type1.getMethods().isEmpty());
		if (hasInterfaceMethods) {
			List<String> proxyInterfaces = new ArrayList<>(Arrays.stream(
					type.getImplementedInterfaces()).map(Type::getDottedName).collect(Collectors.toList()));
			proxyInterfaces.add("org.springframework.aop.SpringProxy");
			proxyInterfaces.add("org.springframework.aop.framework.Advised");
			proxyInterfaces.add("org.springframework.core.DecoratingProxy");
			imageContext.addProxy(proxyInterfaces);
			imageContext.log(ValidatedComponentProcessor.class.getSimpleName() + ": creating proxy for these interfaces: " + proxyInterfaces);
		} else if (!type.isInterface()) {
			// TODO is IS_STATIC always right here?
			imageContext.addAotProxy(type.getDottedName(), Collections.emptyList(), ProxyBits.IS_STATIC);
			imageContext.log(ValidatedComponentProcessor.class.getSimpleName() + ": creating proxy for this class: " + type.getDottedName());
		}
	}

}
