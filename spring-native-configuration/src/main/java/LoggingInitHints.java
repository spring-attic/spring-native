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

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;

@NativeHint(
		initialization = @InitializationHint(types = {
				org.slf4j.spi.LocationAwareLogger.class,
				org.slf4j.Logger.class,
				org.slf4j.event.SubstituteLoggingEvent.class,
				org.slf4j.event.EventRecodingLogger.class,
				org.slf4j.helpers.FormattingTuple.class,
				org.slf4j.helpers.MessageFormatter.class,
				org.slf4j.helpers.SubstituteLogger.class,
				org.slf4j.helpers.Util.class,
				org.slf4j.helpers.NOPLogger.class,
				org.slf4j.helpers.NOPLoggerFactory.class,
				org.slf4j.helpers.SubstituteLoggerFactory.class,
				org.slf4j.impl.StaticLoggerBinder.class,
				org.slf4j.LoggerFactory.class,
				org.slf4j.MDC.class,
				org.apache.commons.logging.LogFactory.class,
		}, typeNames = {
				"org.apache.commons.logging.LogAdapter",
				"org.apache.commons.logging.LogAdapter$1",
				"org.apache.commons.logging.LogAdapter$Slf4jLocationAwareLog",
				"org.apache.commons.logging.LogAdapter$Log4jLog"
		}, packageNames = {
				"ch.qos.logback.core",
				"ch.qos.logback.classic",
				"ch.qos.logback.classic.util",
				"org.apache.logging.log4j",
				"org.apache.logging.slf4j",
				"org.jboss.logging"
		}, initTime = InitializationTime.BUILD))
public class LoggingInitHints implements NativeConfiguration {
}
