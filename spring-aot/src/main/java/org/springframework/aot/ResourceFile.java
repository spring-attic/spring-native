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

package org.springframework.aot;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Brian Clozel
 */
public interface ResourceFile extends GeneratedFile {

	Path NATIVE_IMAGE_PATH = Paths.get("META-INF", "native-image");

	Path NATIVE_CONFIG_PATH = NATIVE_IMAGE_PATH.resolve(Paths.get("org.springframework.aot", "spring-aot"));
	
	Path SPRING_COMPONENTS_PATH = Paths.get("META-INF", "spring.components");

}
