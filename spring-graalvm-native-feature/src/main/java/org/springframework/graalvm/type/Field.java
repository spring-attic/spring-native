/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.graalvm.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

/**
 * @author Christoph Strobl
 */
public class Field {

	private FieldNode node;
	private TypeSystem typeSystem;

	private Lazy<List<Type>> annotations;

	public Field(FieldNode node, TypeSystem typeSystem) {

		this.node = node;
		this.typeSystem = typeSystem;
		this.annotations = Lazy.of(this::resolveAnnotations);
	}

	public List<Type> getAnnotations() {
		return annotations.get();
	}

	private List<Type> resolveAnnotations() {

		List<Type> results = null;

		if (node.visibleAnnotations != null) {
			for (AnnotationNode an : node.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType != null) {
					if (results == null) {
						results = new ArrayList<>();
					}
					results.add(annotationType);
				}
			}
		}
		return results == null ? Collections.emptyList() : results;
	}
}
