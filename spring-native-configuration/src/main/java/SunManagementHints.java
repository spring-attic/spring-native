import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

// See https://github.com/oracle/graal/issues/3875
@NativeHint(options = "-J--add-exports=java.management/sun.management=ALL-UNNAMED")
public class SunManagementHints implements NativeConfiguration {
}
