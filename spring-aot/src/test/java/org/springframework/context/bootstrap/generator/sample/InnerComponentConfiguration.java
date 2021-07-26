package org.springframework.context.bootstrap.generator.sample;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * A configuration with inner classes.
 *
 * @author Stephane Nicoll
 */
@Configuration(proxyBeanMethods = false)
public class InnerComponentConfiguration {

	@Component
	public class NoDependencyComponent {

		public NoDependencyComponent() {

		}
	}

	@Component
	public class EnvironmentAwareComponent {

		public EnvironmentAwareComponent(Environment environment) {

		}
	}
}
