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

package org.springframework.core.type.classreading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link ClassVisitor} implementation for {@link DefaultClassDescriptorReader}
 * @author Brian Clozel
 */
class DefaultClassDescriptorVisitor extends ClassVisitor {

	private final TypeSystem typeSystem;

	private String className = "";

	private int access;

	@Nullable
	private String superClassName;

	private Set<String> interfaceNames = new HashSet<>();

	@Nullable
	private String enclosingClassName;

	private boolean independentInnerClass;

	private Set<String> memberClassNames = new LinkedHashSet<>(4);

	private List<MergedAnnotation<?>> annotations = new ArrayList<>();

	private List<MethodDescriptor> methods = new ArrayList<>();

	@Nullable
	private DefaultClassDescriptor metadata;

	@Nullable
	private Source source;


	DefaultClassDescriptorVisitor(TypeSystem typeSystem) {
		super(SpringAsmInfo.ASM_VERSION);
		this.typeSystem = typeSystem;
	}


	@Override
	public void visit(int version, int access, String name, String signature,
			@Nullable String supername, String[] interfaces) {

		this.className = toClassName(name);
		this.access = access;
		if (supername != null && !isInterface(access)) {
			this.superClassName = toClassName(supername);
		}
		Collections.addAll(this.interfaceNames, interfaces);
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		this.enclosingClassName = toClassName(owner);
	}

	@Override
	public void visitInnerClass(String name, @Nullable String outerName, String innerName,
			int access) {
		if (outerName != null) {
			String className = toClassName(name);
			String outerClassName = toClassName(outerName);
			if (this.className.equals(className)) {
				this.enclosingClassName = outerClassName;
				this.independentInnerClass = ((access & Opcodes.ACC_STATIC) != 0);
			}
			else if (this.className.equals(outerClassName)) {
				this.memberClassNames.add(className);
			}
		}
	}

	@Override
	@Nullable
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return MergedAnnotationReadingVisitor.get(this.typeSystem.getResourceLoader().getClassLoader(), this::getSource,
				descriptor, visible, this.annotations::add);
	}

	@Override
	@Nullable
	public MethodVisitor visitMethod(
			int access, String name, String descriptor, String signature, String[] exceptions) {

		// Skip bridge methods - we're only interested in original
		// annotation-defining user methods. On JDK 8, we'd otherwise run into
		// double detection of the same annotated method...
		if (isBridge(access)) {
			return null;
		}
		return new DefaultMethodDescriptorVisitor(this.typeSystem, this.className, access, name,
				descriptor, methodDescriptor -> this.methods.add(methodDescriptor));
	}

	@Override
	public void visitEnd() {
		this.metadata = new DefaultClassDescriptor(this.typeSystem, this.className, this.access,
				this.superClassName, this.enclosingClassName, this.interfaceNames, this.memberClassNames,
				this.methods, MergedAnnotations.of(this.annotations));
	}

	public DefaultClassDescriptor getDescriptor() {
		Assert.state(this.metadata != null, "AnnotationMetadata not initialized");
		return this.metadata;
	}

	private Source getSource() {
		Source source = this.source;
		if (source == null) {
			source = new Source(this.className);
			this.source = source;
		}
		return source;
	}

	private String toClassName(String name) {
		return ClassUtils.convertResourcePathToClassName(name);
	}

	private boolean isBridge(int access) {
		return (access & Opcodes.ACC_BRIDGE) != 0;
	}

	private boolean isInterface(int access) {
		return (access & Opcodes.ACC_INTERFACE) != 0;
	}

	/**
	 * {@link MergedAnnotation} source.
	 */
	private static final class Source {

		private final String className;

		Source(String className) {
			this.className = className;
		}

		@Override
		public int hashCode() {
			return this.className.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return this.className.equals(((Source) obj).className);
		}

		@Override
		public String toString() {
			return this.className;
		}

	}


}
