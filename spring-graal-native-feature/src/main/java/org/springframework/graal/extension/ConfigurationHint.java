package org.springframework.graal.extension;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(ConfigurationHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationHint {
	
	// TODO change this to a different name - target?
	/**
	 * This is the target of the hint, i.e. what is being hinted about. For example a hint specifying <tt>value=@Import</tt>
	 * is supplying hints about types that must be exposed when @Import is being used.  If no value is specified it
	 * is considered to be a hint about the type upon which the hint annotation is specified.
	 */
	public Class<?> value() default Object.class;

	/**
	 * A set of type infos indicated which types should be made accessible (as resources and/or via reflection)
	 */
	public TypeInfo[] typeInfos() default {};
	
	/**
	 * Names of the annotation attributes on the target type which contain type names (as class references or strings).
	 * The types referenced will be exposed for reflection.
	 */
	public String[] extractTypesFromAttributes() default {};

	public boolean abortIfTypesMissing() default false;
	
	public boolean follow() default false; // TODO get rid of this, infer from types involved (means moving to per type follow setting)
	
}
