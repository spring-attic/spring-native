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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

/**
 * Recognize spring.components that need transactional proxies and register them.
 *
 * @author Andy Clement
 */
public class TransactionalComponentProcessor implements ComponentProcessor {

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		boolean hasTxMethods = type!=null && type.hasTransactionalMethods();
		boolean isInteresting =  (type != null && (type.isTransactional() || hasTxMethods));
		return isInteresting;
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		List<String> transactionalInterfaces = new ArrayList<>();
		for (Type intface: type.getInterfaces()) {
			transactionalInterfaces.add(intface.getDottedName());
		}
		if (transactionalInterfaces.size()!=0) {
			transactionalInterfaces.add("org.springframework.aop.SpringProxy");
			transactionalInterfaces.add("org.springframework.aop.framework.Advised");
			transactionalInterfaces.add("org.springframework.core.DecoratingProxy");
			imageContext.addProxy(transactionalInterfaces);
			imageContext.log("TransactionalComponentProcessor: creating proxy for these interfaces: "+transactionalInterfaces);
		}
		if (!type.isInterface()) {
			// Rationalize why some contexts want one or the other (a class proxy for the class itself vs a jdk proxy for the interfaces it implements)
			// Does it depend on whether the annotation is on the inherited interface methods?
			// Compare events sample and jdbc-tx sample
			// TODO is IS_STATIC always right here?
			imageContext.addAotProxy(type.getDottedName(), Collections.emptyList(), ProxyBits.IS_STATIC);
			imageContext.log("TransactionalComponentProcessor: creating proxy for this class: "+type.getDottedName());
		}
	}

}
