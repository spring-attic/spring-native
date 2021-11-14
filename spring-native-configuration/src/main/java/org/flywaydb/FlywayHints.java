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

package org.flywaydb;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.logging.buffered.BufferedLogCreator;
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = Flyway.class,
		initialization = {
				@InitializationHint(types = {
						FeatureDetector.class,
						NativePathLocationScanner.class,
						BufferedLogCreator.class
				}, initTime = InitializationTime.BUILD)
		},
		types = {
				@TypeHint(types = {Slf4jLogCreator.class})
		},
		resources = {
				@ResourceHint(patterns = {"org/flywaydb/core/internal/version.txt"})
		}
)
public class FlywayHints implements NativeConfiguration {
}
