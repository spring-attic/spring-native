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

package org.springframework.nativex.type;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.nativex.AotOptions;

/**
 * Marker interface for class annotated with {@link org.springframework.nativex.hint.NativeHint} and related annotations,
 * and provide an SPI for providing programmatic hints when declarative annotations are not flexible enough.
 * 
 * @author Andy Clement
 * @author Sebastien Deleuze
 */
public interface NativeConfiguration {

	/**
	 * Implementing this method enables hints on the {@code @NativeConfiguration} implementation to be
	 * conditional on some programmatic test.
	 *
	 * @param aotOptions the AOT options configured for this build.
	 * @return {@code true} if the hints on this configuration should apply.
	 */
	default boolean isValid(AotOptions aotOptions) { return true; }

	/**
	 *
	 * @param registry the registry that allows to register native configuration.
	 * @param aotOptions the AOT options configured for this build.
	 */
	default void computeHints(NativeConfigurationRegistry registry, AotOptions aotOptions) { return; }

}
