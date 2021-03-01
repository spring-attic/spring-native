package com.google.protobuf;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;

@InitializationHint(types = {
		com.google.protobuf.Extension.class,
		com.google.protobuf.ExtensionLite.class,
		com.google.protobuf.ExtensionRegistry.class
}, initTime = InitializationTime.BUILD)
public class ProtobufHints implements NativeConfiguration {
}
