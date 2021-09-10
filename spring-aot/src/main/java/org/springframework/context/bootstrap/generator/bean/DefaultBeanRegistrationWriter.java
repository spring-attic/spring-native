package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
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
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.context.bootstrap.generator.bean.support.MultiStatement;
import org.springframework.context.bootstrap.generator.bean.support.ParameterWriter;
import org.springframework.context.bootstrap.generator.bean.support.TypeWriter;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapClass;
import org.springframework.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.context.bootstrap.generator.infrastructure.ProtectedAccessAnalysis;
import org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionRegistry;
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

	private final BeanValueWriter beanValueWriter;

	private final BeanRegistrationWriterOptions options;

	private final ParameterWriter parameterWriter;

	private int nesting = 0;

	public DefaultBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition, BeanValueWriter beanValueWriter,
			BeanRegistrationWriterOptions options) {
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.beanValueWriter = beanValueWriter;
		this.options = options;
		this.parameterWriter = new ParameterWriter();
	}

	public DefaultBeanRegistrationWriter(String beanName, BeanDefinition beanDefinition, BeanValueWriter beanValueWriter) {
		this(beanName, beanDefinition, beanValueWriter, BeanRegistrationWriterOptions.DEFAULTS);
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

	void writeBeanRegistration(Builder code) {
		initializeBeanDefinitionRegistrar(code);
		code.addStatement(".register(context)");
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
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				registry.addMethod(writeMethod);
			}
		}
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
		BeanInstanceDescriptor descriptor = this.beanValueWriter.getDescriptor();
		boolean shouldDeclareCreator = shouldDeclareCreator(descriptor);
		if (shouldDeclareCreator) {
			handleCreatorReference(code, descriptor.getInstanceCreator().getMember());
		}
		code.add("\n").indent().indent();
		code.add(".instanceSupplier(");
		this.beanValueWriter.writeValueSupplier(code);
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
		writeValue(code, value);
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
		writeValue(code, value);
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
				code.add((this.parameterWriter.writeParameterValue(value, ResolvableType.forInstance(value))));
				code.add(")");
				statements.add(code.build());
			}
		}
	}

	private void writeValue(Builder code, Object value) {
		if (value instanceof BeanDefinition) {
			DefaultBeanRegistrationWriter nestedGenerator = writeNestedBeanDefinition((BeanDefinition) value);
			nestedGenerator.writeBeanDefinition(code);
		}
		if (value instanceof BeanReference) {
			code.add("new $T($S)", RuntimeBeanReference.class, ((BeanReference) value).getBeanName());
		}
		else {
			code.add(this.parameterWriter.writeParameterValue(value, ResolvableType.forInstance(value)));
		}
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

	private boolean isValidName(String name) {
		return name != null && SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name);
	}

	private String determineVariableName(String name) {
		return name + "_".repeat(this.nesting);
	}

}
