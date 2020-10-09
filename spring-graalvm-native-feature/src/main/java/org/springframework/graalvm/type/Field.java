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

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.springframework.graalvm.type.Type.TypeCollector;

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

	public List<Type> getAnnotationTypes() {
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
	
	public List<String> getTypesInSignature() {
		if (node.signature == null) {
			String s = node.desc;
			if (s.endsWith(";")) {
				return Collections.singletonList(Type.fromLdescriptorToSlashed(s));
			} else {
				return Collections.emptyList();
			}
		} else {
			// Pull out all the types from the generic signature
			SignatureReader reader = new SignatureReader(node.signature);
			TypeCollector tc = new TypeCollector();
			reader.accept(tc);
			return tc.getTypes();
		}
	}

	public String getName() {
		return node.name;
	}

	
}
