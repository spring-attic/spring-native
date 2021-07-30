package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.TypeName;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar.InstanceSupplierContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

/**
 * Write the necessary code to {@link #writeInstantiation(Executable) create a bean
 * instance} or {@link #writeInjection(Member, boolean) inject dependencies}.
 * <p/>
 * The writer expects a number of variables to be available and/or accessible:
 * <ul>
 *     <li>{@code context}: the general {@code GenericApplicationContext}</li>
 *     <li>{@code instanceContext}: the {@link InstanceSupplierContext} callback</li>
 *     <li>{@code bean}: the variable that refers to the bean instance</li>
 * </ul>
 *
 * @author Stephane Nicoll
 */
class InjectionPointWriter {

	CodeBlock writeInstantiation(Executable creator) {
		if (creator instanceof Constructor) {
			return write((Constructor<?>) creator);
		}
		if (creator instanceof Method) {
			return writeMethodInstantiation((Method) creator);
		}
		throw new IllegalArgumentException("Could not handle creator " + creator);
	}

	CodeBlock writeInjection(Member member, boolean required) {
		if (member instanceof Method) {
			return writeMethodInjection((Method) member, required);
		}
		if (member instanceof Field) {
			return writeFieldInjection((Field) member, required);
		}
		throw new IllegalArgumentException("Could not handle member " + member);
	}

	private CodeBlock write(Constructor<?> creator) {
		CodeBlock.Builder code = CodeBlock.builder();
		Class<?> declaringType = creator.getDeclaringClass();
		boolean innerClass = isInnerClass(declaringType);

		code.add("instanceContext.constructor(");
		Class<?>[] parameterTypes = Arrays.stream(creator.getParameters()).map(Parameter::getType).toArray(Class<?>[]::new);
		if (innerClass) {
			parameterTypes = Arrays.copyOfRange(parameterTypes, 1, parameterTypes.length);
		}
		code.add(Arrays.stream(parameterTypes).map((d) -> "$T.class").collect(Collectors.joining(", ")), (Object[]) parameterTypes);
		code.add(")\n").indent().indent();
		code.add(".create(context, (attributes) ->");
		List<ParameterResolution> parameters = resolveParameters(creator.getParameters(),
				(index) -> ResolvableType.forConstructorParameter(creator, index));
		if (innerClass) { // Remove the implicit argument
			parameters.remove(0);
		}
		boolean hasAssignment = parameters.stream().anyMatch(ParameterResolution::hasAssignment);
		if (hasAssignment) {
			code.beginControlFlow("");
			parameters.stream().filter(ParameterResolution::hasAssignment).forEach((parameter) -> parameter.applyAssignment(code));
		}
		else {
			code.add(" ");
		}
		if (hasAssignment) {
			code.add("return ");
		}
		if (innerClass) {
			code.add("context.getBean($T.class).new $L(", declaringType.getEnclosingClass(), declaringType.getSimpleName());
		}
		else {
			code.add("new $L(", declaringType.getSimpleName());
		}
		for (int i = 0; i < parameters.size(); i++) {
			parameters.get(i).applyParameter(code);
			if (i < parameters.size() - 1) {
				code.add(", ");
			}
		}
		code.add(")");
		if (hasAssignment) {
			code.add(";\n").unindent().add("}");
		}
		code.add(")").unindent().unindent(); // end of invoke
		return code.build();
	}

	private static boolean isInnerClass(Class<?> type) {
		return type.isMemberClass() && !Modifier.isStatic(type.getModifiers());
	}

	private CodeBlock writeMethodInstantiation(Method injectionPoint) {
		return write(injectionPoint, (code) -> code.add(".create(context, (attributes) ->"), true);
	}

	private CodeBlock writeMethodInjection(Method injectionPoint, boolean required) {
		Consumer<Builder> attributesResolver = (code) -> {
			if (required) {
				code.add(".invoke(context, (attributes) ->");
			}
			else {
				code.add(".resolve(context, false).ifResolved((attributes) ->");
			}
		};
		return write(injectionPoint, attributesResolver, false);
	}

	private CodeBlock write(Method injectionPoint, Consumer<Builder> attributesResolver, boolean instantiation) {
		CodeBlock.Builder code = CodeBlock.builder();
		code.add("instanceContext.method($S, ", injectionPoint.getName());
		Class<?>[] parameterTypes = Arrays.stream(injectionPoint.getParameters()).map(Parameter::getType).toArray(Class<?>[]::new);
		code.add(Arrays.stream(parameterTypes).map((d) -> "$T.class").collect(Collectors.joining(", ")), (Object[]) parameterTypes);
		code.add(")\n").indent().indent();
		attributesResolver.accept(code);
		List<ParameterResolution> parameters = resolveParameters(injectionPoint.getParameters(),
				(index) -> ResolvableType.forMethodParameter(injectionPoint, index));
		boolean hasAssignment = parameters.stream().anyMatch(ParameterResolution::hasAssignment);
		if (hasAssignment) {
			code.beginControlFlow("");
			parameters.stream().filter(ParameterResolution::hasAssignment).forEach((parameter) -> parameter.applyAssignment(code));
		}
		else {
			code.add(" ");
		}
		if (hasAssignment && instantiation) {
			code.add("return ");
		}
		if (instantiation) {
			if (Modifier.isStatic(injectionPoint.getModifiers())) {
				code.add("$T", injectionPoint.getDeclaringClass());
			}
			else {
				code.add("context.getBean($T.class)", injectionPoint.getDeclaringClass());
			}
		}
		else {
			code.add("bean");
		}
		code.add(".$L(", injectionPoint.getName());
		for (int i = 0; i < parameters.size(); i++) {
			parameters.get(i).applyParameter(code);
			if (i < parameters.size() - 1) {
				code.add(", ");
			}
		}
		code.add(")");
		if (hasAssignment) {
			code.add(";\n").unindent().add("}");
		}
		code.add(")").unindent().unindent(); // end of invoke
		return code.build();
	}

	CodeBlock writeFieldInjection(Field injectionPoint, boolean required) {
		CodeBlock.Builder code = CodeBlock.builder();
		code.add("instanceContext.field($S, $T.class", injectionPoint.getName(), injectionPoint.getType());
		code.add(")\n").indent().indent();
		if (required) {
			code.add(".invoke(context, (attributes) ->");
		}
		else {
			code.add(".resolve(context, false).ifResolved((attributes) ->");
		}
		boolean hasAssignment = Modifier.isPrivate(injectionPoint.getModifiers());
		if (hasAssignment) {
			code.beginControlFlow("");
			String fieldName = String.format("%sField", injectionPoint.getName());
			code.addStatement("$T $L = $T.findField($T.class, $S, $T.class)", Field.class, fieldName, ReflectionUtils.class,
					injectionPoint.getDeclaringClass(), injectionPoint.getName(), injectionPoint.getType());
			code.addStatement("$T.makeAccessible($L)", ReflectionUtils.class, fieldName);
			code.addStatement("$T.setField($L, bean, attributes.get(0))", ReflectionUtils.class, fieldName);
			code.unindent().add("}");
		}
		else {
			code.add(" bean.$L = attributes.get(0)", injectionPoint.getName());
		}
		code.add(")");
		return code.build();
	}

	private List<ParameterResolution> resolveParameters(Parameter[] parameters, Function<Integer, ResolvableType> resolvableTypeFactory) {
		List<ParameterResolution> parametersResolution = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			ResolvableType resolvableType = resolvableTypeFactory.apply(i);
			if (resolvableType.hasGenerics()) {
				String parameterName = String.format("attributes%s", i);
				Builder assignment = CodeBlock.builder();
				TypeName typeName = TypeName.get(resolvableType.getType());
				assignment.add("$T $L = attributes.get($L)", typeName, parameterName, i);
				parametersResolution.add(ParameterResolution.ofAssignableParameter(assignment.build(), CodeBlock.of(parameterName)));
			}
			else {
				parametersResolution.add(ParameterResolution.ofParameter(CodeBlock.of("attributes.get($L)", i)));
			}
		}
		return parametersResolution;
	}

}
