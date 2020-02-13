package org.springframework.graal.extension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationHints {

	ConfigurationHint[] value();
}
