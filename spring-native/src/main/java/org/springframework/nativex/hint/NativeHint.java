/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
	 * If no value is specified it is considered to be a hint about the type upon which the hint annotation is specified, unless
	 * the hosting type is an implementation of NativeConfiguration, in which case the hint with no trigger is considered to
	 * encapsulate hints that should always be active.
	 * The trigger is ignored for initialization configuration.
	 * @return the class that acts as a trigger
	 */
	Class<?> trigger() default Object.class;

	/**
	 * A set of type information which indicate which types should be made accessible (as via reflection and/or *.class
	 * resources) if the trigger is active.
	 * @return the type information
	 */
	TypeHint[] types() default {};

	/**
	 * A set of proxy information which indicate which sets of types need a proxy if the trigger is active.
	 * @return the proxy information
	 */
	JdkProxyHint[] jdkProxies() default {};

	/**
	 * A set of class proxy information which describe a class based proxy that should be created at build time
	 * if the trigger is active.
	 * @return the class proxy information
	 */
	AotProxyHint[] aotProxies() default {};
	
	/**
	 * A set of serialization hints indicating which types will be (de)serialized if the trigger is active.
	 * @return the serialization hint
	 */
	SerializationHint[] serializables() default {};
	
	/**
	 * A set of resource information which specify which resources need including if the trigger is active.
	 * @return the resource information
	 */
	ResourceHint[] resources() default {};
	
	/**
	 * A set of initialization information which specify which classes/packages should be initialized
	 * explicitly at runtime/build-time (runtime being GraalVM native image default).
	 * @return the initialization information
	 */
	InitializationHint[] initialization() default {};

	/**
	 * A set of types that have {@link TypeHint} / {@link JdkProxyHint} / etc. annotations on them that should be pulled in as type hints for this one.
	 * Using this mechanism a set of TypeHints can be shared by two hints without duplication (e.g. webflux and webmvc).
	 * @return the types to import
	 */
	Class<?>[] imports() default {};

	/**
	 * GraalVM native options to configure automatically when the hint is active
	 * @return the native options to configure
	 */
	String[] options() default {};
	
	/**
	 * Names of the annotation attributes on the target type which contain type names (as class references or strings)
	 * other than 'value'. For example in {@code @ConditionalOnBean} both value and type can include a type name. This
	 * information is used when determining what must be exposed for reflective access.
	 * @return the attribute names from which types names should be extracted (e.g. name or type )
	 */
	String[] extractTypesFromAttributes() default {};

	/**
	 * Determine if types inferred or specified directly in {@link TypeHint}s should be followed to find further hints.
	 * Some types must obviously be followed such as @Configuration classes but in cases where it is not obvious
	 * follow can be set to true to force it, other types that are automatically followed include selectors and registrars
	 * (full list in Type.shouldFollow()).
	 * @return true if types related to a hint should be followed to discover further hints
	 */
	boolean follow() default false;

	/**
	 * Determine if analysis should stop if the inferred and/or specific types for this hint cannot be found. For example
	 * if an @ConditionalOnClass is used, the conditional class will be inferred. Using this abort option will avoid
	 * analysing deeper into the class if that inferred class is missing. Without this the system will assume the existence
	 * is optional and proceed with deeper analysis.
	 * @return true if analysis should stop when an inferred and/or specific type for this hint cannot be found.
	 */
	boolean abortIfTypesMissing() default false;
}
