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

package org.springframework.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
/**
 * Tests for {@link WebNativeConfigurationProcessor}.
 *
 * @author Andy Clement
 */
class WebNativeConfigurationProcessorTests {

	private static Set<TypeAccess> ALL_MEMBERS;
	{
		ALL_MEMBERS = new HashSet<>(Arrays.asList(TypeAccess.PUBLIC_FIELDS, TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS));
	}
	
	@Test
	void simpleController() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(C1.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(1);
		ClassDescriptor cd = classDescriptors.get(0);
		assertThat(cd.getName()).isEqualTo(Foo.class.getName());
		assertThat(cd.getAccess()).containsAll(ALL_MEMBERS);
	}

	@Test
	void interfaceDefinedController() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(InterfaceDefinedController.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(2);
		ClassDescriptor cd1 = classDescriptors.get(0);
		ClassDescriptor cd2 = classDescriptors.get(1);
		ClassDescriptor fooCD,barCD;
		// They should be Foo and Bar in some order
		if (cd1.getName().endsWith("Foo")) {
			fooCD = cd1;
			barCD = cd2;
		} else {
			fooCD = cd2;
			barCD = cd1;
		}
		assertThat(fooCD.getName()).isEqualTo(Foo.class.getName());
		assertThat(fooCD.getAccess()).containsAll(ALL_MEMBERS);
		assertThat(barCD.getName()).isEqualTo(Bar.class.getName());
		assertThat(barCD.getAccess()).containsAll(ALL_MEMBERS);
	}
	
	@Test
	void rsocketController() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(RSocketController.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(2);
		ClassDescriptor monoCd = ClassDescriptor.of(Mono.class);
		monoCd.setAccess(ALL_MEMBERS);
		assertThat(classDescriptors).contains(monoCd);
		ClassDescriptor messageCd = ClassDescriptor.of(Message.class);
		messageCd.setAccess(ALL_MEMBERS);
		assertThat(classDescriptors).contains(messageCd);
	}

	@Test
	void responseObjectFieldAnalysis() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("noise", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());
		beanFactory.registerBeanDefinition("endpoint", BeanDefinitionBuilder.rootBeanDefinition(AnotherController.class).getBeanDefinition());
		NativeConfigurationRegistry registry = process(beanFactory);
		List<ClassDescriptor> classDescriptors = registry.reflection().toClassDescriptors();
		assertThat(classDescriptors).hasSize(2);
		ClassDescriptor monoCd = ClassDescriptor.of(Foo.class);
		monoCd.setAccess(ALL_MEMBERS);
		assertThat(classDescriptors).contains(monoCd);
		ClassDescriptor messageCd = ClassDescriptor.of(Boo.class);
		messageCd.setAccess(ALL_MEMBERS);
		assertThat(classDescriptors).contains(messageCd);
	}

	private NativeConfigurationRegistry process(DefaultListableBeanFactory beanFactory) {
		NativeConfigurationRegistry registry = new NativeConfigurationRegistry();
		new WebNativeConfigurationProcessor().process(beanFactory, registry);
		return registry;
	}

	@RestController
	public class InterfaceDefinedController implements ControllerDefinition {
	
	    @Override
	    public Foo echo(Bar bar) {
	        return new Foo();
	    }
	
	}

	public interface ControllerDefinition {
	
	    @PostMapping(
	            value = "/echo",
	            produces = { "application/json" },
	            consumes = { "application/json" }
	    )
	    Foo echo(@RequestBody Bar request);
	
	}

	@Controller
	static class C1 {
		@RequestMapping
		public Foo foo() {
			return new Foo();
		}
	}
	
	static class Foo {
	}
	
	static class Bar {
	}


	@Controller
	static class RSocketController {
	
		@MessageMapping("request-response")
		Message requestResponse(Message request) {
			return new Message("SERVER", "RESPONSE");
		}
	
		@MessageMapping("mono-request-response")
		Mono<Message> monoRequestResponse(Message request) {
			return Mono.just(new Message("SERVER", "RESPONSE"));
		}
	}

	static class Message {

		private String string;
		private String string2;

		public Message(String string, String string2) {
			this.setString(string);
			this.setString2(string2);
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public String getString2() {
			return string2;
		}

		public void setString2(String string2) {
			this.string2 = string2;
		}
		
	}
	
	@Controller
	static class AnotherController {
		@GetMapping("/")
		Boo peek() {
			return new Boo();
		}
	}
	
	static class Boo {
		List<Foo> foos;
	}

}
