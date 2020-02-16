package org.springframework.graal.extension;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(ConfigurationHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationHint {
	
	/**
	 * This is the target of the hint, i.e. what is being hinted about. For example a hint on @Import indicating there are
	 * type references in its value fields.
	 */
	public Class<?> value() default Object.class;

	public TypeInfo[] typeInfos() default {};
	
	/**
	 * Names of the annotation attributes on the target type which contain type names (as class references or strings).
	 * The types referenced will be exposed for reflection.
	 */
	public String[] extractTypesFromAttributes() default {};

	public boolean abortIfTypesMissing() default false;
	
	public boolean follow() default false; // TODO get rid of this, infer from types involved (means moving to per type follow setting)
	
}
