package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.bootstrap.generator.BootstrapClass;
import org.springframework.context.bootstrap.generator.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Base {@link BeanRegistrationGenerator} that determines if the registration code has
 * to be written in a separate class due to protected access.
 *
 * @author Stephane Nicoll
 */
public class DefaultBeanRegistrationGenerator implements BeanRegistrationGenerator {

	private final String beanName;

	private final BeanDefinition beanDefinition;

	private final BeanValueWriter beanValueWriter;

	public DefaultBeanRegistrationGenerator(String beanName, BeanDefinition beanDefinition,
			BeanValueWriter beanValueWriter) {
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.beanValueWriter = beanValueWriter;
	}

	@Override
	public void writeBeanRegistration(BootstrapWriterContext context, Builder code) {
		MemberDescriptor<Executable> instanceCreator = this.beanValueWriter.getDescriptor().getInstanceCreator();
		if (isAccessibleFrom(this.beanValueWriter.getDescriptor(), context.getPackageName())) {
			writeBeanRegistration(code);
		}
		else {
			Class<?> beanType = this.beanValueWriter.getDescriptor().getBeanType();
			String protectedPackageName = instanceCreator.getMember().getDeclaringClass().getPackageName();
			BootstrapClass javaFile = context.getBootstrapClass(protectedPackageName);
			String methodName = addBeanRegistrationMethod(javaFile, this.beanName, beanType, this::writeBeanRegistration);
			code.addStatement("$T.$L(context)", javaFile.getClassName(), methodName);
		}
	}

	public void writeBeanRegistration(Builder code) {
		code.add("$T.of($S, ", BeanDefinitionRegistrar.class, this.beanName);
		writeBeanType(code);
		code.add(").instanceSupplier(");
		this.beanValueWriter.writeValueSupplier(code);
		code.add(")");
		handleBeanDefinitionMetadata(code);
		code.addStatement(".register(context)");
	}

	private void handleBeanDefinitionMetadata(Builder code) {
		List<Runnable> customizers = new ArrayList<>();
		if (this.beanDefinition.isPrimary()) {
			customizers.add(() -> code.add(".setPrimary(true)"));
		}
		if (this.beanDefinition instanceof AbstractBeanDefinition
				&& ((AbstractBeanDefinition) this.beanDefinition).isSynthetic()) {
			customizers.add(() -> code.add(".setSynthetic(true)"));
		}
		if (this.beanDefinition.getRole() != BeanDefinition.ROLE_APPLICATION) {
			customizers.add(() -> code.add(".setRole($L)", this.beanDefinition.getRole()));
		}
		if (!customizers.isEmpty()) {
			code.add((".customize((builder) -> builder"));
			customizers.forEach(Runnable::run);
			code.add(")");
		}
	}

	private boolean isAccessibleFrom(BeanInstanceDescriptor descriptor, String packageName) {
		if (!descriptor.getInstanceCreator().isAccessibleFrom(packageName)) {
			return false;
		}
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			if (!injectionPoint.isAccessibleFrom(packageName)) {
				return false;
			}
		}
		return true;
	}

	private void writeBeanType(Builder code) {
		ResolvableType resolvableType = this.beanDefinition.getResolvableType();
		if (resolvableType.hasGenerics()) {
			TypeHelper.generateResolvableTypeFor(code, resolvableType);
		}
		else {
			code.add("$T.class", ClassUtils.getUserClass(this.beanDefinition.getResolvableType().toClass()));
		}
	}

	private String addBeanRegistrationMethod(BootstrapClass javaFile, String beanName,
			Class<?> type, Consumer<Builder> code) {
		String name = registerBeanMethodName(beanName, type);
		MethodSpec.Builder method = MethodSpec.methodBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(GenericApplicationContext.class, "context");
		javaFile.addMethod(method, code);
		return name;
	}

	private String registerBeanMethodName(String beanName, Class<?> type) {
		String target = (isValidName(beanName)) ? beanName : type.getSimpleName();
		return "register" + StringUtils.capitalize(target);
	}

	private boolean isValidName(String className) {
		return SourceVersion.isIdentifier(className) && !SourceVersion.isKeyword(className);
	}

}
