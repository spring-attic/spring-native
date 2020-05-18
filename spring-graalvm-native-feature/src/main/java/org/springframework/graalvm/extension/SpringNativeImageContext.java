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

/**
 * Not yet used. 
 * This would be a context object accessible to a NativeImageConfiguration implementation which may
 * use it to answer questions about the current application being compiled in order to compute optimal 
 * reflection/resource/etc native image configuration.
 * 
 * @author Andy Clement
 */
public interface SpringNativeImageContext {

}
