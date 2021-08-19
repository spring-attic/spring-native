package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.bootstrap.generator.BootstrapClass;
import org.springframework.context.bootstrap.generator.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.ProtectedAccessAnalysis;
import org.springframework.context.bootstrap.generator.bean.support.MultiStatement;
import org.springframework.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.context.bootstrap.generator.bean.support.TypeWriter;
import org.springframework.context.bootstrap.generator.reflect.RuntimeReflectionRegistry;
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

	private static final TypeWriter typeWriter = new TypeWriter();

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
		BeanInstanceDescriptor descriptor = this.beanValueWriter.getDescriptor();
		registerReflectionMetadata(context.getRuntimeReflectionRegistry(), descriptor);

		ProtectedAccessAnalysis analysis = context.getProtectedAccessAnalyzer().analyze(descriptor);
		if (analysis.isAccessible()) {
			writeBeanRegistration(code);
		}
		else {
			String protectedPackageName = analysis.getPrivilegedPackageName();
			BootstrapClass javaFile = context.getBootstrapClass(protectedPackageName);
			MethodSpec method = addBeanRegistrationMethod(descriptor, this::writeBeanRegistration);
			javaFile.addMethod(method);
			code.addStatement("$T.$N(context)", javaFile.getClassName(), method);
		}
	}

	private void registerReflectionMetadata(RuntimeReflectionRegistry registry, BeanInstanceDescriptor descriptor) {
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		registry.addMethod(instanceCreator.getMember());
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				registry.addMethod((Method) member);
			}
			else if (member instanceof Field) {
				registry.addField((Field) member);
			}
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
		MultiStatement statements = new MultiStatement();
		if (this.beanDefinition.isPrimary()) {
			statements.add("bd.setPrimary(true)");
		}
		if (this.beanDefinition instanceof AbstractBeanDefinition
				&& ((AbstractBeanDefinition) this.beanDefinition).isSynthetic()) {
			statements.add("bd.setSynthetic(true)");
		}
		if (this.beanDefinition.getRole() != BeanDefinition.ROLE_APPLICATION) {
			statements.add("bd.setRole($L)", this.beanDefinition.getRole());
		}
		if (this.beanDefinition.hasConstructorArgumentValues()) {
			handleArgumentValues(statements, this.beanDefinition.getConstructorArgumentValues());
		}
		if (statements.isEmpty()) {
			return;
		}
		code.add(statements.toCodeBlock(".customize((bd) ->"));
		code.add(")");
	}

	private void handleArgumentValues(MultiStatement statements, ConstructorArgumentValues constructorArgumentValues) {
		Map<Integer, ValueHolder> values = constructorArgumentValues.getIndexedArgumentValues();
		if (values.size() == 1) {
			Entry<Integer, ValueHolder> entry = values.entrySet().iterator().next();
			statements.add(writeArgumentValue("bd.getConstructorArgumentValues().", entry.getKey(), entry.getValue()));
		}
		else {
			statements.add("$T argumentValues = bd.getConstructorArgumentValues()", ConstructorArgumentValues.class);
			statements.addAll(values.entrySet(), (entry) -> writeArgumentValue("argumentValues.", entry.getKey(), entry.getValue()));
		}
	}

	private CodeBlock writeArgumentValue(String prefix, Integer index, ValueHolder valueHolder) {
		Builder code = CodeBlock.builder();
		code.add(prefix);
		code.add("addIndexedArgumentValue($L, ", index);
		Object value = valueHolder.getValue();
		if (value instanceof BeanReference) {
			code.add("new $T($S)", RuntimeBeanReference.class, ((BeanReference) value).getBeanName());
		}
		else {
			code.add(new ParameterWriter().writeParameterValue(value, ResolvableType.forInstance(value)));
		}
		code.add(")");
		return code.build();
	}

	private void writeBeanType(Builder code) {
		ResolvableType resolvableType = this.beanDefinition.getResolvableType();
		if (resolvableType.hasGenerics()) {
			code.add(typeWriter.generateTypeFor(resolvableType));
		}
		else {
			code.add("$T.class", ClassUtils.getUserClass(this.beanDefinition.getResolvableType().toClass()));
		}
	}

	private MethodSpec addBeanRegistrationMethod(BeanInstanceDescriptor descriptor, Consumer<Builder> code) {
		String name = registerBeanMethodName(descriptor);
		MethodSpec.Builder method = MethodSpec.methodBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(GenericApplicationContext.class, "context");
		CodeBlock.Builder body = CodeBlock.builder();
		code.accept(body);
		method.addCode(body.build());
		return method.build();
	}

	private String registerBeanMethodName(BeanInstanceDescriptor descriptor) {
		Executable member = descriptor.getInstanceCreator().getMember();
		if (member instanceof Method) {
			String target = (isValidName(beanName)) ? beanName : member.getName();
			return String.format("register%s_%s", member.getDeclaringClass().getSimpleName(), target);
		}
		else if (member.getDeclaringClass().getEnclosingClass() != null) {
			String target = (isValidName(beanName)) ? beanName : descriptor.getUserBeanClass().getSimpleName();
			Class<?> enclosingClass = member.getDeclaringClass().getEnclosingClass();
			return String.format("register%s_%s", enclosingClass.getSimpleName(), target);
		}
		else {
			String target = (isValidName(beanName)) ? beanName : descriptor.getUserBeanClass().getSimpleName();
			return "register" + StringUtils.capitalize(target);
		}
	}

	private boolean isValidName(String className) {
		return SourceVersion.isIdentifier(className) && !SourceVersion.isKeyword(className);
	}

}
