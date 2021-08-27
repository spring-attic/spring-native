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

package org.springframework;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

/**
 * Some bean post processors may trigger creation of a proxy for a type. For example
 * {@link PersistenceExceptionTranslationPostProcessor} will cause a proxy for an
 * @Repository class.
 *
 * @author Andy Clement
 */
public class AotProxyRepositoryComponentProcessor implements ComponentProcessor {
	
	// TODO somehow check if spring.aop.proxy-target-class=true?
	
	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		TypeSystem ts = imageContext.getTypeSystem();
		Type component = ts.resolveDotted(componentType,true);
		if (component != null && component.hasAnnotation("Lorg/springframework/stereotype/Repository;", false)) {
			imageContext.log("AotProxyRepositoryComponentProcessor: found @Repository annotation on component "+componentType+": creating class proxy");
			return true;
		}
		return false;
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		imageContext.log("AotProxyRepositoryComponentProcessor: creating proxy for this class: "+componentType);
		imageContext.addAotProxy(componentType, Collections.emptyList(), ProxyBits.IS_STATIC);
	}

}
