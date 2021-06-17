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

import java.util.List;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeName;

/**
 * Recognize components that are using TransactionalEventListener annotated methods.
 *
 * @author Andy Clement
 */
public class TransactionalEventListenerComponentProcessor implements ComponentProcessor {

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		if (!imageContext.getTypeSystem().exists(TypeName.fromClassName("org.springframework.transaction.event.TransactionalEventListener"))) {
			return false;
		}
		List<Method> transactionalEventListenerMethods =
				type.getMethods(m -> m.hasAnnotation("Lorg/springframework/transaction/event/TransactionalEventListener;", false));
		if (!transactionalEventListenerMethods.isEmpty()) {
			imageContext.log("TransactionalEventListenerComponentProcessor: found @TransactionalEventListener within component "+componentType);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		imageContext.addProxy("org.springframework.transaction.event.TransactionalEventListener", "org.springframework.core.annotation.SynthesizedAnnotation");
		imageContext.addReflectiveAccess("org.springframework.transaction.event.TransactionalEventListener", AccessBits.ANNOTATION);
		imageContext.addReflectiveAccess("org.springframework.context.event.EventListener", AccessBits.ANNOTATION);
		imageContext.addReflectiveAccess("org.springframework.transaction.event.TransactionPhase", AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_FIELDS);
	}

}
