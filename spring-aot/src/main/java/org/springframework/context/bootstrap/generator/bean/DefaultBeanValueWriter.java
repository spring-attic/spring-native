package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.TypeName;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

/**
 * Default {@link BeanValueWriter} implementation.
 *
 * @author Stephane Nicoll
 */
public class DefaultBeanValueWriter implements BeanValueWriter {

	private final BeanInstanceDescriptor descriptor;

	private final BeanDefinition beanDefinition;

	private final ClassLoader classLoader;

	private final InjectionPointWriter injectionPointWriter;

	public DefaultBeanValueWriter(BeanInstanceDescriptor descriptor, BeanDefinition beanDefinition,
			ClassLoader classLoader) {
		this.descriptor = descriptor;
		this.beanDefinition = beanDefinition;
		this.classLoader = classLoader;
		this.injectionPointWriter = new InjectionPointWriter();
	}

	@Override
	public BeanInstanceDescriptor getDescriptor() {
		return this.descriptor;
	}

	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	@Override
	public void writeValueSupplier(Builder code) {
		MemberDescriptor<Executable> descriptor = getDescriptor().getInstanceCreator();
		if (descriptor == null) {
			throw new IllegalStateException("Could not handle " + this.beanDefinition + ": no instance creator available");
		}
		Executable member = descriptor.getMember();
		if (member instanceof Constructor) {
			writeBeanInstantiation(code, (Constructor<?>) member, getDescriptor().getInjectionPoints());
		}
		if (member instanceof Method) {
			writeBeanInstantiation(code, (Method) member, getDescriptor().getInjectionPoints());
		}
	}

	private void writeBeanInstantiation(Builder code, Constructor<?> constructor, List<MemberDescriptor<?>> injectionPoints) {
		Class<?> declaringType = getDescriptor().getBeanType();
		boolean innerClass = isInnerClass(declaringType);
		// We need to process any parameters that might hold generic to manage them upfront.
		List<ParameterResolution> parameters = resolveParameters(constructor.getParameters(),
				(i) -> ResolvableType.forConstructorParameter(constructor, i));
		if (innerClass) { // Remove the implicit argument
			parameters.remove(0);
		}
		boolean hasAssignment = parameters.stream().anyMatch(ParameterResolution::hasAssignment);
		boolean multiStatements = hasAssignment || !injectionPoints.isEmpty();
		// Shortcut for common case
		if (!multiStatements && !innerClass && parameters.isEmpty()) {
			code.add("$T::new", getDescriptor().getBeanType());
			return;
		}
		branch(injectionPoints.isEmpty(), () -> code.add("() ->"), () -> code.add("(instanceContext) ->"));
		branch(multiStatements, () -> code.beginControlFlow(""), () -> code.add(" "));
		parameters.stream().filter(ParameterResolution::hasAssignment).forEach((parameter) -> parameter.applyAssignment(code));
		if (hasAssignment && injectionPoints.isEmpty()) {
			code.add("return ");
		}
		else if (!injectionPoints.isEmpty()) {
			code.add("$T bean = ", declaringType);
		}
		branch(innerClass,
				() -> code.add("context.getBean($T.class).new $L(", declaringType.getEnclosingClass(), declaringType.getSimpleName()),
				() -> code.add("new $T(", declaringType));
		for (int i = 0; i < parameters.size(); i++) {
			parameters.get(i).applyParameter(code);
			if (i < parameters.size() - 1) {
				code.add(", ");
			}
		}
		code.add(")");
		if (multiStatements) {
			code.add(";\n");
		}
		if (!injectionPoints.isEmpty()) {
			for (MemberDescriptor<?> injectionPoint : injectionPoints) {
				code.add(this.injectionPointWriter.writeInjection(injectionPoint.getMember(), injectionPoint.isRequired())).add(";\n");
			}
			code.add("return bean;\n");
		}
		if (multiStatements) {
			code.unindent().add("}");
		}
	}

	private static boolean isInnerClass(Class<?> type) {
		return type.isMemberClass() && !Modifier.isStatic(type.getModifiers());
	}

	private void writeBeanInstantiation(Builder code, Method method, List<MemberDescriptor<?>> injectionPoints) {
		// We need to process any parameters that might hold generic to manage them upfront.
		List<ParameterResolution> parameters = resolveParameters(method.getParameters(), (i) -> ResolvableType.forMethodParameter(method, i));
		boolean hasAssignment = parameters.stream().anyMatch(ParameterResolution::hasAssignment);
		boolean multiStatements = hasAssignment || !injectionPoints.isEmpty();
		branch(injectionPoints.isEmpty(), () -> code.add("() ->"), () -> code.add("(instanceContext) ->"));
		branch(multiStatements, () -> code.beginControlFlow(""), () -> code.add(" "));
		parameters.stream().filter(ParameterResolution::hasAssignment).forEach((parameter) -> parameter.applyAssignment(code));
		Class<?> declaringType = method.getDeclaringClass();
		if (hasAssignment && injectionPoints.isEmpty()) {
			code.add("return ");
		}
		else if (!injectionPoints.isEmpty()) {
			code.add("$T bean = ", getDescriptor().getBeanType());
		}
		branch(Modifier.isStatic(method.getModifiers()),
				() -> code.add("$T", declaringType),
				() -> code.add("context.getBean($T.class)", declaringType));
		code.add(".$L(", method.getName());
		for (int i = 0; i < parameters.size(); i++) {
			parameters.get(i).applyParameter(code);
			if (i < parameters.size() - 1) {
				code.add(", ");
			}
		}
		code.add(")");
		if (multiStatements) {
			code.add(";\n");
		}
		if (!injectionPoints.isEmpty()) {
			for (MemberDescriptor<?> injectionPoint : injectionPoints) {
				code.add(this.injectionPointWriter.writeInjection(injectionPoint.getMember(), injectionPoint.isRequired())).add(";\n");
			}
			code.add("return bean;\n");
		}
		if (multiStatements) {
			code.unindent().add("}");
		}
	}

	protected boolean hasCheckedException(Class<?>... exceptionTypes) {
		return Arrays.stream(exceptionTypes).anyMatch((ex) -> !RuntimeException.class.isAssignableFrom(ex));
	}

	protected List<ParameterResolution> resolveParameters(Parameter[] parameters,
			Function<Integer, ResolvableType> parameterTypeFactory) {
		List<ParameterResolution> parametersResolution = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			ResolvableType parameterType = parameterTypeFactory.apply(i);
			ValueHolder userValue = this.beanDefinition.getConstructorArgumentValues().getIndexedArgumentValue(i,
					parameterType.toClass());
			if (userValue != null) {
				Object value = userValue.getValue();
				if (value instanceof BeanReference) {
					parametersResolution.add(resolveParameterBeanDependency(((BeanReference) value).getBeanName(), parameterType));
				}
				else {
					Builder code = CodeBlock.builder();
					writeParameterValue(code, value, parameterType);
					parametersResolution.add(ParameterResolution.ofParameter(code.build()));
				}
			}
			else {
				parametersResolution.add(resolveParameterDependency(parameters[i], parameterType));
			}
		}
		return parametersResolution;
	}

	// workaround to account for the Spring Boot use case for now.
	protected void writeParameterValue(CodeBlock.Builder code, Object value, ResolvableType parameterType) {
		Object targetValue = convertValueIfNecessary(value, parameterType);
		if (parameterType.isArray()) {
			code.add("new $T { ", parameterType.toClass());
			if (targetValue instanceof char[]) {
				char[] array = (char[]) targetValue;
				for (int i = 0; i < array.length; i++) {
					writeParameterValue(code, array[i], ResolvableType.forClass(char.class));
					if (i < array.length - 1) {
						code.add(", ");
					}
				}
			}
			else if (targetValue instanceof String[]) {
				String[] array = (String[]) targetValue;
				for (int i = 0; i < array.length; i++) {
					writeParameterValue(code, array[i], ResolvableType.forClass(String.class));
					if (i < array.length - 1) {
						code.add(", ");
					}
				}
			}
			code.add(" }");
		}
		else if (targetValue instanceof Character) {
			String result = '\'' + characterLiteralWithoutSingleQuotes((Character) targetValue) + '\'';
			code.add(result);
		}
		else if (isPrimitiveOrWrapper(targetValue)) {
			code.add("$L", targetValue);
		}
		else if (targetValue instanceof String) {
			code.add("$S", targetValue);
		}
		else if (targetValue instanceof Class) {
			code.add("$T.class", targetValue);
		}
	}

	private Object convertValueIfNecessary(Object value, ResolvableType resolvableType) {
		if (value instanceof String && Class.class.isAssignableFrom(resolvableType.resolve())) {
			try {
				return ClassUtils.forName(((String) value), this.classLoader);
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Failed to load " + value, ex);
			}
		}
		return value;
	}

	private boolean isPrimitiveOrWrapper(Object value) {
		Class<?> valueType = value.getClass();
		return (valueType.isPrimitive() || valueType == Double.class || valueType == Float.class
				|| valueType == Long.class || valueType == Integer.class || valueType == Short.class
				|| valueType == Character.class || valueType == Byte.class || valueType == Boolean.class);
	}

	protected ParameterResolution resolveParameterDependency(Parameter parameter, ResolvableType parameterType) {
		Class<?> resolvedClass = parameterType.toClass();
		if (ObjectProvider.class.isAssignableFrom(resolvedClass)) {
			return resolveObjectProvider(parameter, parameterType.as(ObjectProvider.class).getGeneric(0), false,
					(assignment) -> assignment.add(""));
		}
		else if (Collection.class.isAssignableFrom(resolvedClass)) {
			String collectors = (Set.class.isAssignableFrom(resolvedClass)) ? "toSet()" : "toList()";
			ResolvableType collectionType = parameterType.as(Collection.class).getGeneric(0);
			return resolveObjectProvider(parameter, collectionType, collectionType.hasGenerics(),
					(assignment) -> assignment.add(".orderedStream().collect($T.$L)", Collectors.class, collectors));
		}
		else if (parameterType.hasGenerics()) {
			return resolveObjectProvider(parameter, parameterType, true,
					(assignment) -> assignment.add(".getObject()"));
		}
		else if (resolvedClass.isAssignableFrom(GenericApplicationContext.class)) {
			return ParameterResolution.ofParameter(CodeBlock.of("context"));
		}
		else if (resolvedClass.isAssignableFrom(ConfigurableListableBeanFactory.class)) {
			return ParameterResolution.ofParameter(CodeBlock.of("context.getBeanFactory()"));
		}
		else if (resolvedClass.isAssignableFrom(ConfigurableEnvironment.class)) {
			return ParameterResolution.ofParameter(CodeBlock.of("context.getEnvironment()"));
		}
		else {
			return resolveParameterBeanDependency(null, parameterType);
		}
	}

	/**
	 * Create a {@link ParameterResolution} for the specified {@link Parameter} when an
	 * {@link ObjectProvider} is required.
	 * @param parameter the parameter to handle
	 * @param targetType the target type for the object provider
	 * @param shouldUseAssignment whether an assignment is required
	 * @param objectProviderAssignment a callback to invoke a method on the {@link ObjectProvider}
	 * @return the parameter resolution
	 */
	private ParameterResolution resolveObjectProvider(Parameter parameter, ResolvableType targetType,
			boolean shouldUseAssignment, Consumer<Builder> objectProviderAssignment) {
		TypeName parameterTypeName = TypeName.get(targetType.getType());
		if (shouldUseAssignment) {
			Builder assignment = CodeBlock.builder();
			String parameterName = String.format("%sProvider", parameter.getName());
			assignment.add("$T<$T> $L = ", ObjectProvider.class, parameterTypeName, parameterName);
			assignment.add("context.getBeanProvider(");
			TypeHelper.generateResolvableTypeFor(assignment, targetType);
			assignment.add(")");
			Builder parameterValue = CodeBlock.builder();
			parameterValue.add("$L", parameterName);
			objectProviderAssignment.accept(parameterValue);
			return ParameterResolution.ofAssignableParameter(assignment.build(), parameterValue.build());
		}
		else {
			Builder code = CodeBlock.builder();
			code.add("context.getBeanProvider(");
			TypeHelper.generateResolvableTypeFor(code, targetType);
			code.add(")");
			objectProviderAssignment.accept(code);
			return ParameterResolution.ofParameter(code.build());
		}
	}

	private ParameterResolution resolveParameterBeanDependency(String beanName, ResolvableType parameterType) {
		CodeBlock.Builder code = CodeBlock.builder();
		Class<?> resolvedClass = parameterType.toClass();
		if (beanName != null) {
			code.add("context.getBean($S, $T.class)", beanName, resolvedClass);
		}
		else {
			code.add("context.getBean($T.class)", resolvedClass);
		}
		return ParameterResolution.ofParameter(code.build());
	}

	// Copied from com.squareup.javapoet.Util
	private static String characterLiteralWithoutSingleQuotes(char c) {
		// see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
		switch (c) {
			case '\b':
				return "\\b"; /* \u0008: backspace (BS) */
			case '\t':
				return "\\t"; /* \u0009: horizontal tab (HT) */
			case '\n':
				return "\\n"; /* \u000a: linefeed (LF) */
			case '\f':
				return "\\f"; /* \u000c: form feed (FF) */
			case '\r':
				return "\\r"; /* \u000d: carriage return (CR) */
			case '\"':
				return "\""; /* \u0022: double quote (") */
			case '\'':
				return "\\'"; /* \u0027: single quote (') */
			case '\\':
				return "\\\\"; /* \u005c: backslash (\) */
			default:
				return Character.isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
		}
	}

	private static void branch(boolean condition, Runnable ifTrue, Runnable ifFalse) {
		if (condition) {
			ifTrue.run();
		}
		else {
			ifFalse.run();
		}
	}

}
