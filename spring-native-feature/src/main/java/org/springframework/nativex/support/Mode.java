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
package org.springframework.nativex.support;

/**
 * Feature operating mode.
 * 
 * @author Andy Clement
 */
public enum Mode {
	REFLECTION, // Default mode, provide everything
	INIT, // initialization-only configuration provided from the feature
	AGENT, // assume agent for 'basic stuff' - feature provides initialization plus other (@Controller analysis?)
}