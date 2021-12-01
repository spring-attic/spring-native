package org.springframework.boot.actuate.autoconfigure.web;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.aot.boot.actuate.web.AotManagementContextFactory;
import org.springframework.aot.context.bootstrap.generator.ApplicationContextAotProcessor;
import org.springframework.aot.context.bootstrap.generator.bean.BeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.DefaultBeanRegistrationWriter;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.BootstrapWriterContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextSuppliers;
import org.springframework.context.support.GenericApplicationContext;

/**
 * A {@link BeanRegistrationWriter} that generates the child context that a
 * {@link ManagementContextFactory} handles.
 *
 * @author Stephane Nicoll
 */
class ManagementContextBeanRegistrationWriter implements BeanRegistrationWriter {

	private static final ClassName MANAGEMENT_BOOSTRAP_CLASS_NAME = ClassName.get("org.springframework.aot", "ManagementContextBoostrapInitializer");

	private final GenericApplicationContext parent;

	private final String beanName;

	private final boolean reactive;

	private final BeanDefinition beanDefinition;

	private final BeanInstanceDescriptor beanInstanceDescriptor;

	ManagementContextBeanRegistrationWriter(GenericApplicationContext parent, String beanName, boolean reactive) {
		this.parent = parent;
		this.beanName = beanName;
		this.reactive = reactive;
		this.beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AotManagementContextFactory.class).getBeanDefinition();
		this.beanInstanceDescriptor = BeanInstanceDescriptor.of(beanDefinition.getResolvableType()).build();
	}

	@Override
	public void writeBeanRegistration(BootstrapWriterContext context, Builder code) {
		ClassName managementBootstrapType = processManagementContext(context);
		new DefaultBeanRegistrationWriter(this.beanName, this.beanDefinition, this.beanInstanceDescriptor) {

			@Override
			protected void writeInstanceSupplier(Builder code) {
				code.add("() -> new $T(", AotManagementContextFactory.class);
				code.add("() -> new $T(), $L", managementBootstrapType, reactive);
				code.add(")");
			}
		}.writeBeanRegistration(context, code);
	}

	/**
	 * Process the dedicated management context using the specified {@code context}.
	 * @param context the writer context to use
	 */
	private ClassName processManagementContext(BootstrapWriterContext context) {
		GenericApplicationContext managementContext = createManagementContext();
		ApplicationContextAotProcessor processor = new ApplicationContextAotProcessor(this.parent.getClassLoader());
		BootstrapWriterContext writerContext = context.fork(MANAGEMENT_BOOSTRAP_CLASS_NAME);
		processor.process(managementContext, writerContext);
		return writerContext.getMainBootstrapClass().getClassName();
	}

	@Override
	public BeanInstanceDescriptor getBeanInstanceDescriptor() {
		return this.beanInstanceDescriptor;
	}

	private GenericApplicationContext createManagementContext() {
		return this.reactive ? ManagementContextSuppliers.Reactive.createManagementContext(this.parent)
				: ManagementContextSuppliers.Servlet.createManagementContext(this.parent);
	}

}
