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

public interface NativeImageConfiguration {
	// TODO any default methods here? They would enable programmatic decisions about what should be exposed
	// They could query the image status (what are the active configurations?) and perhaps make more detailed
	// decisions
	// NativeImageHint computeHints(SpringNativeImageContext snic) { ... }
}
