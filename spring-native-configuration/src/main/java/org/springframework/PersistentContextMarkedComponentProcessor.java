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

import java.util.List;

import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

/**
 * Recognize spring.components that have a PersistenceContext marked field.
 *
 * @author Andy Clement
 */
public class PersistentContextMarkedComponentProcessor implements ComponentProcessor {

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		Type type = imageContext.getTypeSystem().resolveName(componentType);
		return type != null && type.hasAnnotatedField(s -> s.equals("javax.persistence.PersistenceContext"));
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		imageContext.addReflectiveAccess(componentType, Flag.allDeclaredFields);
	}

}
