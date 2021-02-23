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
package org.springframework.nativex.hint;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link NativeHint} annotations should be placed either on Spring configuration/registrar/import-selectors or on some
 * implementation of {@code NativeConfiguration} that the system will discover via a service factory load.
 *
 * <p>This allows hints to be provided for pre-compiled sources that don't yet have hints provided.
 * A {@link NativeHint} determines what needs to be accessible at runtime in a native image (via reflection or via
 * resource access). {@link NativeHint} typically have a trigger that activates them and that would be if a particular
 * piece of configuration/registrar or import-selector is determined to be active in a Spring application. The trigger
 * is either inferred (if the hint is on a piece of Spring configuration/registrar/import-selector) or it can be specified
 * by setting the `trigger` member of the annotation (for hints provided separately to the Spring configuration).
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
@Repeatable(NativeHints.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeHint {

	/**
	 * The class specified here could be a configuration/registrar/import-selectors which will trigger this hint only when
	 * active, or any other class, in that case the hint will be active only if the class is present in the classpath.
	 * If no value is specified it is considered to be a hint about the type upon which the hint annotation is specified.
	 * The trigger is ignore for initialization configuration.
	 * @return the class that acts as a trigger
	 */
	Class<?> trigger() default Object.class;

	/**
	 * A set of type information which indicate which types should be made accessible (as via reflection and/or *.class
	 * resources) if the trigger is active.
	 * @return the type information
	 */
	TypeInfo[] types() default {};

	/**
	 * A set of proxy information which indicate which sets of types need a proxy if the trigger is active.
	 * @return the proxy information
	 */
	ProxyInfo[] proxies() default {};
	
	/**
	 * A set of resource information which specify which resources need including if the trigger is active.
	 * @return the resource information
	 */
	ResourcesInfo[] resources() default {};
	
	/**
	 * A set of initialization information which specify which classes/packages should be initialized
	 * explicitly at runtime/build-time (runtime being GraalVM native image default).
	 * @return the initialization information
	 */
	InitializationInfo[] initialization() default {};

	/**
	 * A set of types that have @TypeInfo/@ProxyInfo/etc. annotations on them that should be pulled in as type information for this hint.
	 * Using this mechanism a set of TypeInfos can be shared by two hints without duplication (e.g. webflux and webmvc).
	 * @return the types to import
	 */
	Class<?>[] imports() default {};
	
	// TODO sort out these 3 members:

	/**
	 * Names of the annotation attributes on the target type which contain type names (as class references or strings).
	 * The types referenced will be exposed for reflection.
	 * @return the types to extract
	 */
	String[] extractTypesFromAttributes() default {};

	boolean abortIfTypesMissing() default false;

	// TODO get rid of this, infer from types involved (means moving to per type follow setting)
	boolean follow() default false;
}
