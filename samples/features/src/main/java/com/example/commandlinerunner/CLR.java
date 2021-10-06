package com.example.commandlinerunner;

import static elsewhere.FooBeanFactory.LazyBean;
import static elsewhere.FooBeanFactory.AnotherLazyBean;
import static elsewhere.FooBeanFactory.AnotherLazyBeanHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	public FooBean foo;
	
	@Bean
	SomeComponent someComponent() {
		return new SomeComponent();
	}
	
	@Autowired
	SomeBean someBean;
	
	@Autowired
	ApplicationContext env;

	@Override
	public void run(String... args) throws Exception {
		if (foo == null) {
			throw new IllegalStateException("foo is not set");
		}
		// Verifying that even though SomeComponent is not marked @Component the
		// field inside gets autowired
		env.getBean(SomeComponent.class).check();
		System.out.println("commandlinerunner running!");

		// Verify lazy bean creation
		LazyBean.messages.add("before");
		LazyBean lazyBean = env.getBean(LazyBean.class);
		LazyBean.messages.add("after");
		System.out.println("lazyBean order: " + LazyBean.messages);

		// Verify lazy bean with ObjectProvider
		AnotherLazyBean.messages.add("before-bean");
		AnotherLazyBeanHolder holder = env.getBean(AnotherLazyBeanHolder.class);
		AnotherLazyBean.messages.add("after-bean");
		System.out.println("lazyProviderBean order: " + AnotherLazyBean.messages);
	}

}

class SomeComponent {
	@Autowired Environment field;
	
	public void check() {
		if (field == null) {
			throw new IllegalStateException("SomeComponent.field is null");
		}
	}
}
