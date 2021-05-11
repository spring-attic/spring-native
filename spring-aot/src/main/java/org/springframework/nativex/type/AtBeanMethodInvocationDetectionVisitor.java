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

package org.springframework.nativex.type;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class visitor looking for invocations of methods where the target is annotated <tt>@Bean</tt>.
 * 
 * @author Andy Clement
 */
class AtBeanMethodInvocationDetectionVisitor extends ClassVisitor {

	private List<String> methodsUsingGetBeanCalls = null;
	private TypeSystem ts;
	private boolean scanning = true;
	private Type type;

	/**
	 * @param inputStream input stream for a class file
	 * @return null if there are no isPresent() checks otherwise a list of types
	 *         isPresent() checks are being made upon (may be empty if isPresent()
	 *         checks are made but the target cannot be determined)
	 */
	public static List<String> run(TypeSystem ts, InputStream inputStream) {
		try {
			AtBeanMethodInvocationDetectionVisitor node = new AtBeanMethodInvocationDetectionVisitor(ts, Opcodes.ASM9);
			ClassReader reader = new ClassReader(inputStream);
			reader.accept(node, ClassReader.SKIP_DEBUG);
			return node.methodsUsingGetBeanCalls;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private AtBeanMethodInvocationDetectionVisitor(TypeSystem ts, int api) {
		super(api);
		this.ts = ts;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.type = ts.resolveSlashed(name);
		if (!this.type.isAtConfiguration()) {
			scanning = false;
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (scanning && worthVisiting(access) && this.type.isAtBeanMethod(name, descriptor)) {
			return new AtBeanInvocationFindingMethodVisitor(name, this.api);
		} else {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}

	private boolean worthVisiting(int access) {
		return (access & Opcodes.ACC_BRIDGE)==0 &&
				// TODO shouldn't really ignore synthetics (lambdas may be making invocations)
			   (access & Opcodes.ACC_SYNTHETIC)==0;
	}

	class AtBeanInvocationFindingMethodVisitor extends MethodVisitor {

		private String methodName;

		public AtBeanInvocationFindingMethodVisitor(String name, int api) {
			super(api);
			this.methodName = name;
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean itface) {
Type ownerType = ts.resolveSlashed(owner);
			if (ownerType.isAtConfiguration()) {
				boolean isAtBeanMethod = ownerType.isAtBeanMethod(name,descriptor);
				if (isAtBeanMethod) {
					// Filter out valid super.myAtBeanMethod() use cases
					if (!name.equals(methodName)) {
						if (methodsUsingGetBeanCalls == null) {
							methodsUsingGetBeanCalls = new ArrayList<>();
						}
						methodsUsingGetBeanCalls.add(methodName);
					}
				}
			} else {
				super.visitMethodInsn(opcode, owner, name, descriptor, itface);
			}
		}

	}
}