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

package org.h2;

import org.h2.store.fs.async.FilePathAsync;
import org.h2.store.fs.disk.FilePathDisk;
import org.h2.store.fs.mem.FilePathMem;
import org.h2.store.fs.niomapped.FilePathNioMapped;
import org.h2.store.fs.niomem.FilePathNioMem;
import org.h2.store.fs.retry.FilePathRetryOnInterrupt;
import org.h2.store.fs.split.FilePathSplit;
import org.h2.store.fs.zip.FilePathZip;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = Driver.class, types =
		@TypeHint( types = {
				FilePathDisk.class,
				FilePathMem.class,
				FilePathNioMem.class,
				FilePathSplit.class,
				FilePathNioMem.class,
				FilePathNioMapped.class,
				FilePathAsync.class,
				FilePathZip.class,
				FilePathRetryOnInterrupt.class
		}, typeNames= { "org.h2.store.fs.FilePathMemLZF","org.h2.store.fs.FilePathNioMemLZF" }),
		initialization = @InitializationHint(types = org.h2.util.Bits.class, initTime = InitializationTime.BUILD)
		)
public class H2Hints implements NativeConfiguration {
}
