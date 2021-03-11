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

package org.springframework.boot.autoconfigure.transaction;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;

@NativeHint(trigger = TransactionAutoConfiguration.class, types = {
		@TypeHint(types = {
				TransactionManager.class,
				ProxyTransactionManagementConfiguration.class,
				AbstractTransactionManagementConfiguration.class
		}, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS),
		@TypeHint(types = TransactionDefinition.class, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_FIELDS)
}, abortIfTypesMissing = true)
public class TransactionHints implements NativeConfiguration {
}
