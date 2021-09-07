package org.springframework.context.bootstrap.generator.sample.callback;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
public class NestedImportConfiguration {

	@Configuration(proxyBeanMethods = false)
	@Import(ImportAwareConfiguration.class)
	public static class Nested {

	}

}
