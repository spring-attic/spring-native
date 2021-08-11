package hello;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;

@TypeHint(types = Greeting.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.PUBLIC_METHODS)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringAotApplication.run(Application.class, args);
	}

}
