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

package org.springframework.aot.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.bean.support.MultiStatement;
import org.springframework.aot.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.aot.context.bootstrap.generator.bean.support.TypeWriter;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.ProtectedAccessAnalysis;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Base {@link BeanRegistrationWriter} that determines if the registration code has
 * to be written in a separate class due to protected access.
 *
 * @author Stephane Nicoll
 */
public class DefaultBeanRegistrationWriter implements BeanRegistrationWriter {

	private static final TypeWriter typeWriter = new TypeWriter();

	private final String beanName;

	private final BeanDefinition beanDefinition;

	private final BeanInstanceDescriptor beanInstanceDescriptor;

	private final BeanRegistrationWriterOptions options;

	private final ParameterWriter parameterWriter;

	private int nesting = 0;

	public DefaultBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition,
			BeanInstanceDescriptor beanInstanceDescriptor, BeanRegistrationWriterOptions options) {
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.beanInstanceDescriptor = beanInstanceDescriptor;
		this.options = options;
		this.parameterWriter = new ParameterWriter(this::writeNestedBeanDefinition);
	}

	public DefaultBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition,
			BeanInstanceDescriptor beanInstanceDescriptor) {
		this(beanName, beanDefinition, beanInstanceDescriptor, BeanRegistrationWriterOptions.DEFAULTS);
	}

	@Override
	public BeanInstanceDescriptor getBeanInstanceDescriptor() {
		return this.beanInstanceDescriptor;
	}

	@Override
	public void writeBeanRegistration(BootstrapWriterContext context, Builder code) {
		ProtectedAccessAnalysis analysis = context.getProtectedAccessAnalyzer().analyze(this.beanInstanceDescriptor);
		if (analysis.isAccessible()) {
			writeBeanRegistration(code);
		}
		else {
			String protectedPackageName = analysis.getPrivilegedPackageName();
			BootstrapClass javaFile = context.getBootstrapClass(protectedPackageName);
			MethodSpec method = addBeanRegistrationMethod(this::writeBeanRegistration);
			javaFile.addMethod(method);
			code.addStatement("$T.$N(context)", javaFile.getClassName(), method);
		}
	}

	/**
	 * Return the predicate to use to include Bean Definition
	 * {@link AttributeAccessor attributes}.
	 * @return the bean definition's attributes include filter
	 */
	protected Predicate<String> getAttributeFilter() {
		return (candidate) -> false;
	}

	/**
	 * Specify if the creator {@link Executable} should be defined. By default, a creator
	 * is specified if the {@code instanceSupplier} callback is used with an
	 * {@code instanceContext} callback.
	 * @param descriptor the bean descriptor
	 * @return {@code true} to declare the creator
	 */
	protected boolean shouldDeclareCreator(BeanInstanceDescriptor descriptor) {
		Executable executable = (descriptor.getInstanceCreator() != null)
				? descriptor.getInstanceCreator().getMember() : null;
		if (executable instanceof Method) {
			return true;
		}
		if (executable instanceof Constructor) {
			int minArgs = isInnerClass(descriptor.getUserBeanClass()) ? 2 : 1;
			return executable.getParameterCount() >= minArgs;
		}
		return false;
	}

	/**
	 * Write the statements to instantiate the bean.
	 * @param code the code builder to use
	 */
	protected void writeInstanceSupplier(Builder code) {
		new DefaultBeanInstanceSupplierWriter(this.beanInstanceDescriptor,
				this.beanDefinition).writeInstanceSupplier(code);
	}

	void writeBeanRegistration(Builder code) {
		initializeBeanDefinitionRegistrar(code);
		code.addStatement(".register(context)");
	}

	void writeBeanDefinition(Builder code) {
		initializeBeanDefinitionRegistrar(code);
		code.add(".toBeanDefinition()");
	}

	private void initializeBeanDefinitionRegistrar(Builder code) {
		code.add("$T", BeanDefinitionRegistrar.class);
		if (this.beanName != null) {
			code.add(".of($S, ", this.beanName);
		}
		else {
			code.add(".inner(");
		}
		writeBeanType(code);
		code.add(")");
		boolean shouldDeclareCreator = shouldDeclareCreator(this.beanInstanceDescriptor);
		if (shouldDeclareCreator) {
			handleCreatorReference(code, this.beanInstanceDescriptor.getInstanceCreator().getMember());
		}
		code.add("\n").indent().indent();
		code.add(".instanceSupplier(");
		writeInstanceSupplier(code);
		code.add(")").unindent().unindent(); ;
		handleBeanDefinitionMetadata(code);
	}

	private static boolean isInnerClass(Class<?> type) {
		return type.isMemberClass() && !java.lang.reflect.Modifier.isStatic(type.getModifiers());
	}

	private void handleCreatorReference(Builder code, Executable creator) {
		if (creator instanceof Method) {
			code.add(".withFactoryMethod($T.class, $S", creator.getDeclaringClass(), creator.getName());
			if (creator.getParameterCount() > 0) {
				code.add(", ");
			}
		}
		else {
			code.add(".withConstructor(");
		}
		code.add(this.parameterWriter.writeExecutableParameterTypes(creator));
		code.add(")");
	}

	private void handleBeanDefinitionMetadata(Builder code) {
		String bdVariable = determineVariableName("bd");
		MultiStatement statements = new MultiStatement();
		if (this.beanDefinition.isPrimary()) {
			statements.add("$L.setPrimary(true)", bdVariable);
		}
		String scope = this.beanDefinition.getScope();
		if (StringUtils.hasText(scope) && !ConfigurableBeanFactory.SCOPE_SINGLETON.equals(scope)) {
			statements.add("$L.setScope($S)", bdVariable, scope);
		}
		if (this.beanDefinition.isLazyInit()) {
			statements.add("$L.setLazyInit(true)", bdVariable);
		}
		if (!this.beanDefinition.isAutowireCandidate()) {
			statements.add("$L.setAutowireCandidate(false)", bdVariable);
		}
		if (this.beanDefinition instanceof AbstractBeanDefinition
				&& ((AbstractBeanDefinition) this.beanDefinition).isSynthetic()) {
			statements.add("$L.setSynthetic(true)", bdVariable);
		}
		if (this.beanDefinition.getRole() != BeanDefinition.ROLE_APPLICATION) {
			statements.add("$L.setRole($L)", bdVariable, this.beanDefinition.getRole());
		}
		if (this.beanDefinition.hasConstructorArgumentValues()) {
			handleArgumentValues(statements, bdVariable, this.beanDefinition.getConstructorArgumentValues());
		}
		if (this.beanDefinition.hasPropertyValues()) {
			handlePropertyValues(statements, bdVariable, this.beanDefinition.getPropertyValues());
		}
		if (this.beanDefinition.attributeNames().length > 0) {
			handleAttributes(statements, bdVariable);
		}
		if (statements.isEmpty()) {
			return;
		}
		code.add(statements.toCodeBlock(".customize((" + bdVariable + ") ->"));
		code.add(")");
	}

	private void handleArgumentValues(MultiStatement statements, String bdVariable,
			ConstructorArgumentValues constructorArgumentValues) {
		Map<Integer, ValueHolder> values = constructorArgumentValues.getIndexedArgumentValues();
		if (values.size() == 1) {
			Entry<Integer, ValueHolder> entry = values.entrySet().iterator().next();
			statements.add(writeArgumentValue(bdVariable + ".getConstructorArgumentValues().",
					entry.getKey(), entry.getValue()));
		}
		else {
			String avVariable = determineVariableName("argumentValues");
			statements.add("$T $L = $L.getConstructorArgumentValues()", ConstructorArgumentValues.class, avVariable, bdVariable);
			statements.addAll(values.entrySet(), (entry) -> writeArgumentValue(avVariable + ".",
					entry.getKey(), entry.getValue()));
		}
	}

	private CodeBlock writeArgumentValue(String prefix, Integer index, ValueHolder valueHolder) {
		Builder code = CodeBlock.builder();
		code.add(prefix);
		code.add("addIndexedArgumentValue($L, ", index);
		Object value = valueHolder.getValue();
		code.add(this.parameterWriter.writeParameterValue(value));
		code.add(")");
		return code.build();
	}

	private void handlePropertyValues(MultiStatement statements, String bdVariable,
			PropertyValues propertyValues) {
		PropertyValue[] properties = propertyValues.getPropertyValues();
		if (properties.length == 1) {
			statements.add(writePropertyValue(bdVariable + ".getPropertyValues().", properties[0]));
		}
		else {
			String pvVariable = determineVariableName("propertyValues");
			statements.add("$T $L = $L.getPropertyValues()", MutablePropertyValues.class, pvVariable, bdVariable);
			for (PropertyValue property : properties) {
				statements.add(writePropertyValue(pvVariable + ".", property));
			}
		}
	}

	private CodeBlock writePropertyValue(String prefix, PropertyValue property) {
		Builder code = CodeBlock.builder();
		code.add(prefix);
		code.add("addPropertyValue($S, ", property.getName());
		Object value = property.getValue();
		code.add(this.parameterWriter.writeParameterValue(value));
		code.add(")");
		return code.build();
	}

	private void handleAttributes(MultiStatement statements, String bdVariable) {
		String[] attributeNames = this.beanDefinition.attributeNames();
		Predicate<String> filter = getAttributeFilter();
		for (String attributeName : attributeNames) {
			if (filter.test(attributeName)) {
				Object value = this.beanDefinition.getAttribute(attributeName);
				Builder code = CodeBlock.builder();
				code.add("$L.setAttribute($S, ", bdVariable, attributeName);
				code.add((this.parameterWriter.writeParameterValue(value)));
				code.add(")");
				statements.add(code.build());
			}
		}
	}

	private void writeNestedBeanDefinition(BeanDefinition beanDefinition, Builder code) {
		DefaultBeanRegistrationWriter nestedGenerator = writeNestedBeanDefinition(beanDefinition);
		nestedGenerator.writeBeanDefinition(code);
	}

	private DefaultBeanRegistrationWriter writeNestedBeanDefinition(BeanDefinition value) {
		// TODO: stop assuming default implementation
		DefaultBeanRegistrationWriter writer = (DefaultBeanRegistrationWriter) this.options.getWriterFor(null, value);
		if (writer == null) {
			throw new IllegalStateException("No bean registration writer available for nested bean definition " + value);
		}
		writer.nesting = this.nesting + 1;
		return writer;
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

	private MethodSpec addBeanRegistrationMethod(Consumer<Builder> code) {
		String name = registerBeanMethodName();
		MethodSpec.Builder method = MethodSpec.methodBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(GenericApplicationContext.class, "context");
		CodeBlock.Builder body = CodeBlock.builder();
		code.accept(body);
		method.addCode(body.build());
		return method.build();
	}

	private String registerBeanMethodName() {
		Executable member = this.beanInstanceDescriptor.getInstanceCreator().getMember();
		if (member instanceof Method) {
			String target = (isValidName(beanName)) ? beanName : member.getName();
			return String.format("register%s_%s", member.getDeclaringClass().getSimpleName(), target);
		}
		else if (member.getDeclaringClass().getEnclosingClass() != null) {
			String target = (isValidName(beanName)) ? beanName :
					this.beanInstanceDescriptor.getUserBeanClass().getSimpleName();
			Class<?> enclosingClass = member.getDeclaringClass().getEnclosingClass();
			return String.format("register%s_%s", enclosingClass.getSimpleName(), target);
		}
		else {
			String target = (isValidName(beanName)) ? beanName :
					this.beanInstanceDescriptor.getUserBeanClass().getSimpleName();
			return "register" + StringUtils.capitalize(target);
		}
	}

	private boolean isValidName(String name) {
		return name != null && SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name);
	}

	private String determineVariableName(String name) {
		return name + "_".repeat(this.nesting);
	}

}
