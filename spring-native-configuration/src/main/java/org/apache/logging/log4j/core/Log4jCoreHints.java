package org.apache.logging.log4j.core;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;


/**
 * Minimal Log4j configuration to avoid crashing during compilation.
 * TODO See https://github.com/spring-projects-experimental/spring-native/issues/115
 *
 * @author Sebastien Deleuze
 */
@NativeHint(
	trigger = org.apache.logging.log4j.core.Logger.class, options = "-Dlog4j2.disable.jmx=true",
		initialization = @InitializationHint(typeNames = {
				"com.oracle.truffle.js.scriptengine.GraalJSEngineFactory",
				"com.oracle.truffle.js.scriptengine.GraalJSScriptEngine"
		}, initTime = InitializationTime.BUILD)
)
public class Log4jCoreHints implements NativeConfiguration {
}
