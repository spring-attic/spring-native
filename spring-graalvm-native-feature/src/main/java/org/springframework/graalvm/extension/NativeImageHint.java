/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graalvm.extension;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * NativeImageHint annotations should be placed either on Spring configuration/registrar/import-selectors or on some
 * implementation of NativeImageConfiguration that the system will discover via a service factory load.
 * This allows hints to be provided for pre-compiled sources that don't yet have hints provided.
 * A NativeImageHint determines what needs to be accessible at runtime in a native image (via reflection or via
 * resource access). NativeImageHints typically have a trigger that activates them and that would be if a particular
 * piece of configuration/registrar or import-selector is determined to be active in a Spring application. The trigger
 * is either inferred (if the hint is on a piece of Spring configuration/registrar/import-selector) or it can be specified
 * by setting the `trigger` member of the annotation (for hints provided separately to the Spring configuration).
 * 
 * A NativeImageHint will specify a number of TypeInfo annotations each of which might specify a different type of access
 * for some set of types.
 * 
 * @author Andy Clement
 */
@Repeatable(NativeImageHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeImageHint {
	
	// TODO change this to a different name - target?
	/**
	 * This is the target of the hint, i.e. what is being hinted about. For example a hint specifying <tt>value=@Import</tt>
	 * is supplying hints about types that must be exposed when @Import is being used.  If no value is specified it
	 * is considered to be a hint about the type upon which the hint annotation is specified.
	 */
	public Class<?> trigger() default Object.class;

	/**
	 * A set of type infos indicated which types should be made accessible (as resources and/or via reflection)
	 */
	public TypeInfo[] typeInfos() default {};
	
	// TODO sort out these 3 members:
	/**
	 * Names of the annotation attributes on the target type which contain type names (as class references or strings).
	 * The types referenced will be exposed for reflection.
	 */
	public String[] extractTypesFromAttributes() default {};

	public boolean abortIfTypesMissing() default false;
	
	public boolean follow() default false; // TODO get rid of this, infer from types involved (means moving to per type follow setting)
	
}
