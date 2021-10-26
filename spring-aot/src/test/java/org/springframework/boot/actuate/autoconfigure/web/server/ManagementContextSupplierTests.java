package org.springframework.boot.actuate.autoconfigure.web.server;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ManagementContextSuppliers}.
 *
 * @author Stephane Nicoll
 */
class ManagementContextSupplierTests {

	@Test
	void createServletManagementContextWithWebServerFactoryInParent() {
		GenericApplicationContext parent = new GenericApplicationContext();
		parent.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(
				TomcatServletWebServerFactory.class).getBeanDefinition());
		GenericApplicationContext context = ManagementContextSuppliers.Servlet.createManagementContext(parent);
		assertThat(context.containsBeanDefinition("ServletWebServerFactory")).isTrue();
		assertThat(context.getBeanDefinition("ServletWebServerFactory").getResolvableType().toClass()).isEqualTo(TomcatServletWebServerFactory.class);
	}

	@Test
	void createServletManagementContextWithoutWebServerFactoryInParent() {
		GenericApplicationContext parent = new GenericApplicationContext();
		GenericApplicationContext context = ManagementContextSuppliers.Servlet.createManagementContext(parent);
		assertThat(context.containsBeanDefinition("ServletWebServerFactory")).isFalse();
	}

	@Test
	void createReactiveManagementContextWithWebServerFactoryInParent() {
		GenericApplicationContext parent = new GenericApplicationContext();
		parent.registerBeanDefinition("test", BeanDefinitionBuilder.rootBeanDefinition(
				TomcatReactiveWebServerFactory.class).getBeanDefinition());
		GenericApplicationContext context = ManagementContextSuppliers.Reactive.createManagementContext(parent);
		assertThat(context.containsBeanDefinition("ReactiveWebServerFactory")).isTrue();
		assertThat(context.getBeanDefinition("ReactiveWebServerFactory").getResolvableType().toClass()).isEqualTo(TomcatReactiveWebServerFactory.class);
	}

	@Test
	void createReactiveManagementContextWithoutWebServerFactoryInParent() {
		GenericApplicationContext parent = new GenericApplicationContext();
		GenericApplicationContext context = ManagementContextSuppliers.Reactive.createManagementContext(parent);
		assertThat(context.containsBeanDefinition("ReactiveWebServerFactory")).isFalse();
	}

}
