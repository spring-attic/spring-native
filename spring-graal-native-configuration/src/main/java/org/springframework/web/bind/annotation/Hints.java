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
package org.springframework.web.bind.annotation;

import org.springframework.graal.extension.NativeImageHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

// TODO do these need to be more conditional? The triggers are either of the web stack configurations - do those
// autoconfigs share a common ancestor that these could trigger off? Although, of course these will only be exposed
// if they are on the classpath - why would you have the web jar on the classpath if not using these?
@NativeImageHint(typeInfos = {
		// TODO What about some way to say "all annotations in this package"
		@TypeInfo(types= {
				ModelAttribute.class,
				InitBinder.class,
				ResponseBody.class,RequestBody.class,RestController.class, RequestParam.class,
				PathVariable.class,
				RequestMapping.class,GetMapping.class,PostMapping.class,PutMapping.class,DeleteMapping.class,PatchMapping.class},access=AccessBits.CLASS|AccessBits.DECLARED_METHODS)
	})
public class Hints implements NativeImageConfiguration {
}
