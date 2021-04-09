package com.example.commandlinerunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.TypeHint;
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