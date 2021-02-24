package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;

@NativeHint(types = @TypeInfo(types = Greeting.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.PUBLIC_METHODS))
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
