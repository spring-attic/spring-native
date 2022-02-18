package org.apache.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


/**
 * @author Josh Long
 * @author Sebastien Deleuze
 */
@NativeHint(
	trigger = LogManager.class,
	types = @TypeHint(types = {
		ReusableMessageFactory.class, DefaultFlowMessageFactory.class, ParameterizedMessageFactory.class, ReusableMessageFactory.class
	})
)
public class Log4jApiHints implements NativeConfiguration {
}
