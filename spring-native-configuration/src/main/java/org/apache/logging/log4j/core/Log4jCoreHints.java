package org.apache.logging.log4j.core;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;


/**
 * @author Sebastien Deleuze
 */
@NativeHint(
	trigger = org.apache.logging.log4j.core.Logger.class, options = "-Dlog4j2.disable.jmx=true",
		initialization = @InitializationHint(typeNames = {
				"com.oracle.truffle.js.scriptengine.GraalJSEngineFactory"
		}, initTime = InitializationTime.BUILD)
)
public class Log4jCoreHints implements NativeConfiguration {
}
