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

package org.springframework.nativex.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import org.springframework.asm.SpringAsmInfo;
import org.springframework.nativex.type.Type.TypeCollector;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 */
public class Field {

	private FieldNode node;
	private TypeSystem typeSystem;

	private Lazy<List<Type>> annotations;
	private Lazy<List<String>> signatureTypes;

	public Field(FieldNode node, TypeSystem typeSystem) {
		this.node = node;
		this.typeSystem = typeSystem;
		this.annotations = Lazy.of(this::resolveAnnotations);
		this.signatureTypes = Lazy.of(this::resolveSignatureTypes);
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

	public Map<String, String> getAnnotationValuesInHierarchy(String LdescriptorLookingFor) {
		if (node.visibleAnnotations == null) {
			return Collections.emptyMap();
		}
		Map<String, String> collector = new HashMap<>();
		getAnnotationValuesInHierarchy(LdescriptorLookingFor, new ArrayList<>(), collector);
		return collector;
	}

	public void getAnnotationValuesInHierarchy(String lookingFor, List<String> seen, Map<String, String> collector) {

		if (node.visibleAnnotations != null) {
			for (AnnotationNode anno : node.visibleAnnotations) {
				if (seen.contains(anno.desc))
					continue;
				seen.add(anno.desc);
				// logger.debug("Comparing "+anno.desc+" with "+lookingFor);
				if (anno.desc.equals(lookingFor) && anno.values != null) {
					List<Object> os = anno.values;
					for (int i = 0; i < os.size(); i += 2) {
						if(os.get(i+1) instanceof List) {

							List<String> resolvedAttributeValues = new ArrayList<>();

							for(Object attributeValue : (List)os.get(i+1)) {

								if(!ObjectUtils.isArray(attributeValue)) {
									resolvedAttributeValues.add(attributeValue.toString());
									continue;
								}

								String attributeValueStringRepresentation = "";
								String[] args = (String[]) attributeValue;
								if(args[0].startsWith("L") && args[0].endsWith(";")) {
									Type resolve = typeSystem.Lresolve(args[0], true);
									if(resolve != null) {
										attributeValueStringRepresentation = resolve.getDottedName();
									}
								}
								if(args.length > 1) {
									attributeValueStringRepresentation += ("." + args[1]);
								}
								resolvedAttributeValues.add(attributeValueStringRepresentation);
							}
							collector.put(os.get(i).toString(), StringUtils.collectionToDelimitedString(resolvedAttributeValues, ";"));
						} else {
							collector.put(os.get(i).toString(), os.get(i + 1).toString());
						}
					}
				}
				try {
					Type resolve = typeSystem.Lresolve(anno.desc);
					resolve.getAnnotationValuesInHierarchy(lookingFor, seen, collector);
				} catch (MissingTypeException mte) {
					// not on classpath, that's ok
				}
			}
		}
	}



	/**
	 * @return {@literal true} if considered a final field ({@link Opcodes#ACC_FINAL}).
	 */
	public boolean isFinal() {
		return (node.access & Opcodes.ACC_FINAL) != 0;
	}

	/**
	 * @return {@literal true} if considered synthetic ({@link Opcodes#ACC_SYNTHETIC}).
	 */
	public boolean isSynthetic() {
		return (node.access & Opcodes.ACC_SYNTHETIC) != 0;
	}

	public Type getType() {
		return getSignature().stream().map(typeSystem::resolveSlashed).findFirst().orElse(null);
	}

	private List<String> resolveSignatureTypes() {
		if (node.signature == null) {
			String s = node.desc;
			List<String> types = new ArrayList<>();
			if (s.startsWith("[")) {
				s = s.substring(s.lastIndexOf("[")+1);
			}
			if (s.length()!=1) { // check for primitive
				types.add(Type.fromLdescriptorToSlashed(s));
			}
			return types;
		} else {
			// Pull out all the types from the generic signature
			SignatureReader reader = new SignatureReader(node.signature);
			FieldSignatureCollector tc = new FieldSignatureCollector();
			reader.accept(tc);
			return tc.getTypes();
		}
	}

	public List<String> getSignature() {
		return new ArrayList<>(signatureTypes.get());
	}

	public Set<String> getTypesInSignature() {
		if (node.signature == null) {
			String s = node.desc;
			Set<String> types = new TreeSet<>();
			if (s.startsWith("[")) {
				s = s.substring(s.lastIndexOf("[")+1);
			}
			if (s.length()!=1) { // check for primitive
				types.add(Type.fromLdescriptorToSlashed(s));
			}
			return types;
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

	static class FieldSignatureCollector extends SignatureVisitor {

		List<String> types = null;

		public FieldSignatureCollector() {
			super(SpringAsmInfo.ASM_VERSION);
		}

		@Override
		public void visitClassType(String name) {
			if (types == null) {
				types = new ArrayList<>();
			}
			types.add(name);
		}

		public List<String> getTypes() {
			if (types == null) {
				return Collections.emptyList();
			} else {
				return types;
			}
		}
	}
}
