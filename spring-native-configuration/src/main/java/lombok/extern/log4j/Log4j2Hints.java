package lombok.extern.log4j;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(
	trigger = Log4j2.class,
	types = @TypeHint(types = {
		org.apache.logging.log4j.message.DefaultFlowMessageFactory.class,
		org.apache.logging.log4j.message.ReusableMessageFactory.class
	})
)
public class Log4j2Hints implements NativeConfiguration {
}
