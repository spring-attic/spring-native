package org.springframework.boot.test.autoconfigure.json;

import com.google.gson.Gson;

import org.springframework.boot.test.json.AbstractJsonMarshalTester;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.GsonTester;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonbTester;
import org.springframework.core.ResolvableType;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Native hints for Spring Boot's json testers.
 *
 * @author Stephane Nicoll
 */
@NativeHint(trigger = BasicJsonTester.class, types = @TypeHint(types = BasicJsonTester.class,
		access = TypeAccess.DECLARED_CONSTRUCTORS,
		methods = @MethodHint(name = "initialize", parameterTypes = Class.class)))
@NativeHint(trigger = JacksonTester.class, types = @TypeHint(types = JacksonTester.class,
		access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = Gson.class, types = @TypeHint(types = GsonTester.class,
		access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = JsonbTester.class, types = @TypeHint(types = JsonbTester.class,
		access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = AbstractJsonMarshalTester.class, types = @TypeHint(types = AbstractJsonMarshalTester.class,
		methods = @MethodHint(name = "initialize", parameterTypes = { Class.class, ResolvableType.class })))
public class JsonTesterHints implements NativeConfiguration {
}
