package org.springframework.graal.extension;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(ConfigurationHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationHint {
	
	public Class<?> value() default Object.class;

	public TypeInfo[] typeInfos() default {};
	
	//public Class<?>[] types() default {};

	// Due to visibility reasons may not be able to directly refer to .class - so use fully qualified names here
	//public String[] typeNames() default {};

	/**
	 * Names of the annotation attributes on the annotation this hint is attached to which contain type names
	 * that should be extracted (and considered inferred).
	 */
	public String[] extractTypesFromAttributes() default {};

	public boolean abortIfTypesMissing() default false;
	
	public boolean follow() default false; // TODO get rid of this, infer from types involved (means moving to per type follow setting)
	
}
