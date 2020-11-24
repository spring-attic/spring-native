package org.springframework.boot.autoconfigure.web.servlet;

import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http11.Http11NioProtocol;

import org.springframework.boot.autoconfigure.web.CommonWebInfos;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.nativex.extension.MethodInfo;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(trigger= Tomcat.class, typeInfos = {
		@TypeInfo(types = TomcatEmbeddedWebappClassLoader.class),
		@TypeInfo(types = AbstractProtocol.class, methods = @MethodInfo(name = "getLocalPort"),access=AccessBits.CLASS),
		@TypeInfo(types = Http11NioProtocol.class)
}, importInfos = CommonWebInfos.class)
public class TomcatHints implements NativeImageConfiguration  {
}
