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
package org.springframework.boot.logging;

import java.util.Collections;
import java.util.List;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.support.ConfigOptions;
import org.springframework.nativex.type.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.TypeSystem;

public class LogLevelHints implements NativeImageConfiguration {
	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {
		if (ConfigOptions.checkForFunctionalModeFromHint()) {
			HintDeclaration hint = new HintDeclaration();
			hint.addDependantType("org.springframework.boot.logging.LogLevel",
				new AccessDescriptor(AccessBits.ALL));
			return Collections.singletonList(hint);
		}
		return Collections.emptyList();
	}
}
