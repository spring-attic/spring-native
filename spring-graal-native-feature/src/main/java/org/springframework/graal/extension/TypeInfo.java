package org.springframework.graal.extension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.graal.type.AccessBits;

//@Repeatable(TypeInfos.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeInfo {
	
	public Class<?>[] types() default {};

	public String[] typeNames() default {};
	
	public int access() default AccessBits.ALL;
}
