/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.graalvm.buildtools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.core.ParameterizedTypeReference;

/**
 * @author Dave Syer
 *
 */
public class CachingSubclassSpec implements Comparable<CachingSubclassSpec> {

	private static Log logger = LogFactory.getLog(CachingSubclassSpec.class);

	private TypeSpec generated;

	private String pkg;

	private Class<?> configurationType;

	private ElementUtils utils;

	private ClassName className;

	private boolean enabled = true;

	public CachingSubclassSpec(ElementUtils utils, Class<?> type, Imports imports) {
		this.utils = utils;
		this.className = toCachingSubclassNameFromConfigurationName(type);
		this.pkg = ClassName.get(type).packageName();
		type = imports.getImports().containsKey(type) && type.isAnnotation()
				? imports.getImports().get(type).iterator().next() : type;
		this.configurationType = type;
		for (Class<?> imported : utils.getTypesFromAnnotation(type, SpringClassNames.IMPORT.toString(), "value")) {
			imports.addImport(type, imported);
		}
	}

	public Class<?> getConfigurationType() {
		return configurationType;
	}

	public void setConfigurationType(Class<?> configurationType) {
		this.configurationType = configurationType;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public TypeSpec getCachingSubclass() {
		if (generated == null) {
			this.generated = createCachingSubclass(configurationType);
		}
		return generated;
	}

	public String getPackage() {
		return pkg;
	}

	public void setPackage(String pkg) {
		this.pkg = pkg;
	}

	private TypeSpec createCachingSubclass(Class<?> type) {
		try {
			List<Method> beanMethods = getBeanMethods(type);
			logger.info("Bean methods " + beanMethods + ", " + utils.isProxyBeanMethods(type));
			if (beanMethods.isEmpty() || !utils.isProxyBeanMethods(type)) {
				return null;
			}
			else {
				TypeSpec subclass = subclass(type, beanMethods);
				return subclass;
			}
		}
		catch (Throwable e) {
			logger.info("Cannot reflect on: " + type.getName());
			return null;
		}
	}

	public static ClassName toCachingSubclassNameFromConfigurationName(Class<?> type) {
		return toCachingSubclassNameFromConfigurationName(ClassName.get(type));
	}

	public static ClassName toCachingSubclassNameFromConfigurationName(ClassName type) {
		String name = type.simpleName();
		if (type.enclosingClassName() != null) {
			name = type.enclosingClassName().simpleName() + "_" + name;
		}
		return ClassName.get(type.packageName(), name + "Initializer");
	}

	private TypeSpec subclass(Class<?> type, List<Method> beanMethods) {
		Builder subclass = TypeSpec.classBuilder(type.getSimpleName().replace("$", "_") + "Cached").superclass(type);
		subclass.addModifiers(Modifier.PUBLIC);
		subclass.addField(methodCache());
		// subclass.addAnnotation(AnnotationSpec.builder(SpringClassNames.CONFIGURATION)
		// .addMember("proxyBeanMethods", "$L", false).build());
		Constructor<?> constructor = getConstructor(type);
		if (constructor.getParameterTypes().length > 0) {
			subclass.addMethod(constructor(constructor));
		}
		for (Method method : beanMethods) {
			if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
				// no overrides for static methods
				continue;
			}
			if (java.lang.reflect.Modifier.isPrivate(method.getReturnType().getModifiers())) {
				// no overrides for private types
				continue;
			}
			subclass.addMethod(methodSpec(method));
		}
		return subclass.build();
	}

	private MethodSpec constructor(Constructor<?> method) {
		MethodSpec.Builder spec = MethodSpec.constructorBuilder();
		spec.addModifiers(Modifier.PUBLIC);
		ParameterSpec params = new ParameterSpec();
		for (Parameter param : method.getParameters()) {
			Type type = param.getParameterizedType();
			Type rawType = rawType(param.getType(), type);
			params.add(param.getName(), rawType);
			spec.addParameter(rawType, param.getName());
		}
		spec.addStatement("super(" + params.format() + ")");
		return spec.build();
	}

	private MethodSpec methodSpec(Method method) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder(method.getName());
		spec.addAnnotation(Override.class);
		spec.addAnnotation(SpringClassNames.BEAN);
		Type returnType = method.getGenericReturnType();
		if (returnType instanceof ParameterizedType) {
			spec.addAnnotation(
					AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build());
		}
		if (returnType instanceof TypeVariable) {
			returnType = method.getReturnType();
		}
		spec.addModifiers(Modifier.PUBLIC);
		spec.returns(rawType(utils.getReturnType(method), returnType));
		ParameterSpec params = new ParameterSpec();
		for (Parameter param : method.getParameters()) {
			Type type = param.getParameterizedType();
			Type rawType = rawType(param.getType(), type);
			params.add(param.getName(), rawType);
			spec.addParameter(rawType, param.getName());
		}
		String key = key(method);
		spec.beginControlFlow("if (!METHODS.containsKey($S))", key);
		spec.addStatement("METHODS.put($S, super.$L(" + params.format() + "))", key(method), method.getName());
		spec.endControlFlow();
		spec.addStatement("return ($T) METHODS.get($S)", returnType, key(method));
		return spec.build();
	}

	private Type rawType(Class<?> rawType, Type type) {
		Type result = type;
		if (type instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) type).getActualTypeArguments();
			if (Stream.of(types).anyMatch(t -> t instanceof TypeVariable)) {
				// We don't care any more. Sigh. Spring Session.
				result = rawType;
			}
		}
		else if (type instanceof TypeVariable) {
			result = rawType;
		}
		return result;
	}

	private FieldSpec methodCache() {
		FieldSpec.Builder builder = FieldSpec.builder(new ParameterizedTypeReference<Map<String, Object>>() {
		}.getType(), "METHODS", Modifier.PRIVATE);
		builder.initializer("new $T<>()", ConcurrentHashMap.class);
		return builder.build();
	}

	private List<Method> getBeanMethods(Class<?> type) {
		Set<String> seen = new HashSet<>();
		List<Method> beanMethods = new ArrayList<>();
		while (type != null) {
			for (Method candidate : type.getDeclaredMethods()) {
				if (isBeanMethod(candidate) && !candidate.isBridge() && seen.add(key(candidate))) {
					beanMethods.add(candidate);
				}
			}
			// Ensure we include all inherited methods
			type = utils.getSuperType(type);
		}
		return beanMethods;
	}

	private String key(Method method) {
		return method.getName() + "(" + Arrays.asList(method.getParameterTypes()).stream().map(type -> type.getName())
				.collect(Collectors.joining(",")) + ")";
	}

	private Constructor<?> getConstructor(Class<?> type) {
		Set<String> seen = new HashSet<>();
		List<Constructor<?>> methods = new ArrayList<>();
		for (Constructor<?> candidate : type.getDeclaredConstructors()) {
			if (seen.add(candidate.toString())) {
				methods.add(candidate);
			}
		}
		// TODO: pick one that is explicitly autowired?
		if (methods.isEmpty()) {
			System.err.println("Wah: " + type);
		}
		return methods.get(0);
	}

	private boolean isBeanMethod(Method element) {
		int modifiers = element.getModifiers();
		if (!utils.hasAnnotation(element, SpringClassNames.BEAN.toString())) {
			return false;
		}
		if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
			return false;
		}
		return true;
	}

	static class ParameterSpec {

		private StringBuilder builder = new StringBuilder();

		private List<TypeName> types = new ArrayList<>();

		public void add(String added, Type... types) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(added);
			for (Type type : types) {
				this.types.add(TypeName.get(type));
			}
		}

		public String format() {
			return builder.toString();
		}

		public Object[] prepend(Object... objects) {
			Object[] result = new Object[types.size() + objects.length];
			for (int i = 0; i < objects.length; i++) {
				result[i] = objects[i];
			}
			for (int i = 0; i < types.size(); i++) {
				result[objects.length + i] = types.get(i);
			}
			return result;
		}

	}

	@Override
	public String toString() {
		return "InitializerSpec:" + className.toString();
	}

	public ClassName getClassName() {
		return className;
	}

	@Override
	public int compareTo(CachingSubclassSpec o) {
		return this.className.compareTo(o.getClassName());
	}

	static class DeferredConfigurations extends AutoConfigurations {

		protected DeferredConfigurations(Collection<Class<?>> classes) {
			super(classes);
		}

		public Class<?>[] list() {
			return getClasses().toArray(new Class<?>[0]);
		}

	}

}
