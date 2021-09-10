package org.springframework.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.ProtectedAccessAnalysis.ProtectedElement;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * Analyze a {@link BeanInstanceDescriptor} for potential protected access.
 *
 * @author Stephane Nicoll
 */
public class ProtectedAccessAnalyzer {

	private final String packageName;

	/**
	 * Create a new instance for the specified target package name
	 * @param packageName the name of the package to use
	 */
	public ProtectedAccessAnalyzer(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Analyze the specified {@link BeanInstanceDescriptor} for potential protected
	 * access.
	 * @param descriptor the descriptor to analyze
	 * @return the {@link ProtectedAccessAnalysis} for the given descriptor
	 */
	public ProtectedAccessAnalysis analyze(BeanInstanceDescriptor descriptor) {
		List<ProtectedElement> elements = new ArrayList<>();
		elements.addAll(analyze(descriptor.getBeanType()));
		if (descriptor.getInstanceCreator() != null) {
			elements.addAll(analyze(descriptor.getInstanceCreator().getMember()));
		}
		descriptor.getInjectionPoints().stream().map(MemberDescriptor::getMember)
				.forEach((member) -> elements.addAll(analyze(member)));
		return new ProtectedAccessAnalysis(elements);
	}

	private List<ProtectedElement> analyze(ResolvableType target) {
		List<ProtectedElement> elements = new ArrayList<>();
		analyze(target, target, elements);
		return elements;
	}

	private List<ProtectedElement> analyze(Member member) {
		List<ProtectedElement> protectedElements = new ArrayList<>();
		protectedElements.addAll(analyze(ResolvableType.forClass(member.getDeclaringClass())));
		if (!isAccessible(member.getModifiers(), member.getDeclaringClass().getPackageName())) {
			protectedElements.add(ProtectedElement.of(member.getDeclaringClass(), member));
		}
		if (member instanceof Constructor) {
			Constructor<?> constructor = (Constructor<?>) member;
			protectedElements.addAll(analyze(constructor.getParameters(), (i) -> ResolvableType.forConstructorParameter(constructor, i)));
		}
		else if (member instanceof Field) {
			protectedElements.addAll(analyze(ResolvableType.forField((Field) member)));
		}
		else if (member instanceof Method) {
			Method method = (Method) member;
			if (!isAccessible(method.getReturnType())) {
				protectedElements.add(ProtectedElement.of(method.getReturnType(), member));
			}
			protectedElements.addAll(analyze(method.getParameters(), (i) -> ResolvableType.forMethodParameter(method, i)));
		}
		return protectedElements;
	}

	private List<ProtectedElement> analyze(Parameter[] parameters, Function<Integer, ResolvableType> parameterTypeFactory) {
		List<ProtectedElement> protectedElements = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			protectedElements.addAll(analyze(parameterTypeFactory.apply(i)));
		}
		return protectedElements;
	}

	private void analyze(ResolvableType rootType, ResolvableType target, List<ProtectedElement> elements) {
		// resolve to the actual class as the proxy won't have the same characteristics
		ResolvableType nonProxyTarget = target.as(ClassUtils.getUserClass(target.toClass()));
		if (!isAccessible(nonProxyTarget.toClass())) {
			elements.add(ProtectedElement.of(nonProxyTarget.toClass(), nonProxyTarget));
		}
		Class<?> declaringClass = nonProxyTarget.toClass().getDeclaringClass();
		if (declaringClass != null) {
			if (!isAccessible(declaringClass)) {
				elements.add(ProtectedElement.of(declaringClass, nonProxyTarget));
			}
		}
		if (nonProxyTarget.hasGenerics()) {
			for (ResolvableType generic : nonProxyTarget.getGenerics()) {
				analyze(rootType, generic, elements);
			}
		}
	}

	private boolean isAccessible(Class<?> target) {
		Class<?> candidate = ClassUtils.getUserClass(target);
		return isAccessible(candidate.getModifiers(), candidate.getPackageName());
	}

	private boolean isAccessible(int modifiers, String actualPackageName) {
		return Modifier.isPublic(modifiers) || this.packageName.equals(actualPackageName);
	}

}
