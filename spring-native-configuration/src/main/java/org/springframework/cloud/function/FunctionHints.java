package org.springframework.cloud.function;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;

@InitializationHint(typeNames = "org.springframework.cloud.function.web.function.FunctionEndpointInitializer", initTime = InitializationTime.BUILD)
public class FunctionHints implements NativeConfiguration {
}
