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

package org.springframework.aot.factories;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.build.BootstrapContributor;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.context.ResourceFile;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.nativex.AotOptions;

import static org.springframework.asm.SpringAsmInfo.ASM_VERSION;

/**
 * Modify the {@link SpringApplication} on the classpath with enhancements for
 * Aot and put the result in the project output folder.
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public class ModifiedSpringApplicationContributor implements BootstrapContributor {

	private static final Log logger = LogFactory.getLog(ModifiedSpringApplicationContributor.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

	@Override
	public void contribute(BuildContext context, AotOptions aotOptions) {
		Resource resource = new DefaultResourceLoader(context.getClassLoader())
				.getResource("org/springframework/boot/SpringApplication.class");
		SpringApplicationClassRewriter ca = modify(resource);
		byte[] bytes = ca.getBytes();
		if (bytes != null) {
			Path path = Paths.get("org/springframework/boot/SpringApplication.class");
			context.addResources(new ResourceFile() {
				@Override
				public void writeTo(Path resourcesPath) throws IOException {
					Path relativeFolder = path.getParent();
					Path filename = path.getFileName();
					Path absoluteFolder = resourcesPath.resolve(relativeFolder);
					Files.createDirectories(absoluteFolder);
					Path absolutePath = absoluteFolder.resolve(filename);
					try (FileOutputStream fos = new FileOutputStream(absolutePath.toFile())) {
						fos.write(bytes);
					}
				}
			});
		}
	}

	private SpringApplicationClassRewriter modify(Resource resource) {
		try {
			ClassReader cr = new ClassReader(resource.getInputStream());
			SpringApplicationClassRewriter springApplicationClassRewriter = new SpringApplicationClassRewriter();
			cr.accept(springApplicationClassRewriter, 0);
			return springApplicationClassRewriter;
		} catch (IOException ioe) {
			throw new IllegalStateException("Problem accessing and rewriting SpringApplication.class", ioe);
		}
	}

	static class SpringApplicationClassRewriter extends ClassVisitor implements Opcodes {

		public SpringApplicationClassRewriter() {
			// Introducing branches so must recompute frames
			super(ASM_VERSION, new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS));
		}

		ModifyConstructor modifyConstructor = null;
		ModifyLoadMethod modifyLoadMethod = null;
		ModifyGetSpringFactoriesInstances modifyGetSpringFactoriesInstances = null;
		boolean mainRemoved = false;

		public byte[] getBytes() {
			if (!mainRemoved) {
				// Class was already patched
				return null;
			}
			if (modifyConstructor == null) {
				throw new IllegalStateException(
						"Unable to find SpringApplication(ResourceLoader, Class[]) constructor to modify");
			} else {
				modifyConstructor.assertSuccessful();
			}
			if (modifyLoadMethod == null) {
				throw new IllegalStateException("Unable to find load(ApplicationContext, Object[]) method to modify");
			} else {
				modifyLoadMethod.assertSuccessful();
			}
			if (modifyGetSpringFactoriesInstances == null) {
				throw new IllegalStateException("Unable to find getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) method to modify");
			} else {
				modifyGetSpringFactoriesInstances.assertSuccessful();
			}
			return ((ClassWriter) cv).toByteArray();
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (name.equals("<init>")
					&& desc.equals("(Lorg/springframework/core/io/ResourceLoader;[Ljava/lang/Class;)V")) {
				// Modifying SpringApplication(ResourceLoader, Class[]) constructor
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				modifyConstructor = new ModifyConstructor(mv);
				return modifyConstructor;
			} else if (name.equals("load")
					&& desc.equals("(Lorg/springframework/context/ApplicationContext;[Ljava/lang/Object;)V")) {
				// Modifying load(ApplicationContext,Object[]) method
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				modifyLoadMethod = new ModifyLoadMethod(mv);
				return modifyLoadMethod;
			} else if (name.equals("getSpringFactoriesInstances")
					&& desc.equals("(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/util/Collection;")) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				modifyGetSpringFactoriesInstances = new ModifyGetSpringFactoriesInstances(mv);
				return modifyGetSpringFactoriesInstances;
			} else if (name.equals("main")) { // To avoid "Unable to find a single main class" errors
				mainRemoved = true;
				return null;
			} else {
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
		}

		class ModifyConstructor extends MethodVisitor implements Opcodes {

			private List<String> successfulPatches = new ArrayList<>();

			public ModifyConstructor(MethodVisitor mv) {
				super(ASM_VERSION, mv);
			}

			public void assertSuccessful() {
				if (!successfulPatches.contains("patched-constructor-primarysources")) {
					throw new IllegalStateException(
							"Missing patch on primarySources setting in SpringApplication constructor. Successful patches were: "
									+ successfulPatches);
				}
				if (!successfulPatches.contains("patched-constructor-endbit")) {
					throw new IllegalStateException(
							"Missing patch at the end of SpringApplication constructor. Successful patches were: "
									+ successfulPatches);
				}
			}

			Label after;

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				if (opcode == INVOKESTATIC && name.equals("notNull")) {
					// This code is rewriting the Assert.notNull
					// first, remove the parameters that would have been passed to the notNull call
					mv.visitInsn(POP);
					mv.visitInsn(POP);
					mv.visitMethodInsn(INVOKESTATIC, "org/springframework/nativex/AotModeDetector", "isAotModeEnabled",
							"()Z", false);
					Label normal = new Label();
					mv.visitJumpInsn(IFEQ, normal);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(NEW, "java/util/LinkedHashSet");
					mv.visitInsn(DUP);
					mv.visitInsn(ICONST_1);
					mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
					mv.visitInsn(DUP);
					mv.visitInsn(ICONST_0);
					mv.visitLdcInsn(Type.getObjectType("java/lang/Object"));
					mv.visitInsn(AASTORE);
					mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList",
							"([Ljava/lang/Object;)Ljava/util/List;", false);
					mv.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedHashSet", "<init>", "(Ljava/util/Collection;)V",
							false);
					after = new Label();
					mv.visitJumpInsn(GOTO, after);
					mv.visitLabel(normal);
				} else {
					super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				}
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
				// assuming the next PUTFIELD primarySources is where the jump target (after)
				// should be placed
				if (opcode == PUTFIELD && name.equals("primarySources") && after != null) {
					mv.visitLabel(after);
					after = null;
					successfulPatches.add("patched-constructor-primarysources");
				}
				super.visitFieldInsn(opcode, owner, name, descriptor);
			}

			@Override
			public void visitInsn(int opcode) {
				if (opcode == RETURN) {
					mv.visitMethodInsn(INVOKESTATIC, "org/springframework/nativex/AotModeDetector", "isRunningAotTests",
							"()Z", false);
					Label elseIfClause = new Label();
					mv.visitJumpInsn(IFEQ, elseIfClause);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitLdcInsn(Type.getType("Lorg/springframework/context/ApplicationContextInitializer;"));
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/boot/SpringApplication", "getSpringFactoriesInstances", "(Ljava/lang/Class;)Ljava/util/Collection;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/boot/SpringApplication", "setInitializers", "(Ljava/util/Collection;)V", false);
					Label end = new Label();
					mv.visitJumpInsn(GOTO, end);
					mv.visitLabel(elseIfClause);
					mv.visitMethodInsn(INVOKESTATIC, "org/springframework/nativex/AotModeDetector", "isAotModeEnabled",
							"()Z", false);
					Label elseClause = new Label();
					mv.visitJumpInsn(IFEQ, elseClause);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETSTATIC, "org/springframework/aot/SpringApplicationAotUtils", "AOT_FACTORY",
							"Lorg/springframework/boot/ApplicationContextFactory;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/boot/SpringApplication",
							"setApplicationContextFactory", "(Lorg/springframework/boot/ApplicationContextFactory;)V",
							false);
					mv.visitTypeInsn(NEW, "java/util/ArrayList");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
					mv.visitVarInsn(ASTORE, 3);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitMethodInsn(INVOKESTATIC, "org/springframework/aot/SpringApplicationAotUtils", "getBootstrapInitializer", "()Lorg/springframework/context/ApplicationContextInitializer;", false);
					mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitTypeInsn(NEW, "org/springframework/boot/autoconfigure/logging/ConditionEvaluationReportLoggingListener");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/autoconfigure/logging/ConditionEvaluationReportLoggingListener", "<init>", "()V", false);
					mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitLdcInsn(Type.getType("Lorg/springframework/context/ApplicationContextInitializer;"));
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/boot/SpringApplication", "getSpringFactoriesInstances", "(Ljava/lang/Class;)Ljava/util/Collection;", false);
					mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/boot/SpringApplication", "setInitializers", "(Ljava/util/Collection;)V", false);
					mv.visitJumpInsn(GOTO, end);
					mv.visitLabel(elseClause);
					mv.visitMethodInsn(INVOKESTATIC, "org/springframework/nativex/AotModeDetector", "isGeneratingAotTests", "()Z", false);
					mv.visitJumpInsn(IFNE, end);
					mv.visitLabel(end);
					successfulPatches.add("patched-constructor-endbit");
				}
				super.visitInsn(opcode);
			}

		}

		class ModifyLoadMethod extends MethodVisitor implements Opcodes {

			private boolean successfulPatch = false;

			public ModifyLoadMethod(MethodVisitor mv) {
				super(ASM_VERSION, mv);
			}

			public void assertSuccessful() {
				if (!successfulPatch) {
					throw new IllegalStateException("Unable to patch on SpringApplication load method");
				}
			}

			private Label end;

			// At the start of the load method, make the call to isAotModeEnabled and
			// jump to the end of the method if true
			public void visitCode() {
				super.visitMethodInsn(INVOKESTATIC, "org/springframework/nativex/AotModeDetector", "isAotModeEnabled",
						"()Z", false);
				end = new Label();
				super.visitJumpInsn(IFNE, end);
			}

			// At the end of the load method, place the label to indicate the end
			@Override
			public void visitInsn(int opcode) {
				if (opcode == RETURN && end != null) {
					mv.visitLabel(end);
					successfulPatch = true;
				}
				super.visitInsn(opcode);
			}

		}

		class ModifyGetSpringFactoriesInstances extends MethodVisitor implements Opcodes {

			private boolean successfulPatch = false;

			public ModifyGetSpringFactoriesInstances(MethodVisitor mv) {
				super(ASM_VERSION, mv);
			}

			public void assertSuccessful() {
				if (!successfulPatch) {
					throw new IllegalStateException("Unable to patch on SpringApplication getSpringFactoriesInstances method");
				}
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				if (name.equals("createSpringFactoriesInstances")) {
					mv.visitInsn(POP2);
					mv.visitInsn(POP2);
					mv.visitInsn(POP2);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ARRAYLENGTH);
					Label argsNotEmptyLabel = new Label();
					mv.visitJumpInsn(IFNE, argsNotEmptyLabel);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitMethodInsn(INVOKESTATIC, "org/springframework/core/io/support/SpringFactoriesLoader", "loadFactories", "(Ljava/lang/Class;Ljava/lang/ClassLoader;)Ljava/util/List;", false);
					Label endOfIf = new Label();
					mv.visitJumpInsn(GOTO,endOfIf);
					mv.visitLabel(argsNotEmptyLabel);
					mv.visitVarInsn(ALOAD,0); // this
					mv.visitVarInsn(ALOAD,1); // Class type
					mv.visitVarInsn(ALOAD,2); // parameterTypes
					mv.visitVarInsn(ALOAD,4); // classLoader
					mv.visitVarInsn(ALOAD,3); // args
					mv.visitVarInsn(ALOAD,5); // names
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/boot/SpringApplication", "createSpringFactoriesInstances", "(Ljava/lang/Class;[Ljava/lang/Class;Ljava/lang/ClassLoader;[Ljava/lang/Object;Ljava/util/Set;)Ljava/util/List;", false);
					mv.visitLabel(endOfIf);
					successfulPatch = true;
				} else {
					super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				}
			}
		}
	}

}
