package org.apache.catalina;

import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http11.Http11NioProtocol;

import org.springframework.boot.autoconfigure.web.CommonWebInfos;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger= Tomcat.class, types = {
		@TypeHint(types = TomcatEmbeddedWebappClassLoader.class),
		@TypeHint(types = AbstractProtocol.class, methods = @MethodHint(name = "getLocalPort"),access=AccessBits.CLASS),
		@TypeHint(types = Http11NioProtocol.class),
}, initialization = {
		@InitializationHint(types = {
				org.apache.catalina.servlets.DefaultServlet.class,
				org.apache.catalina.Globals.class
		}, initTime = InitializationTime.BUILD)
}, imports = CommonWebInfos.class)
public class TomcatHints implements NativeConfiguration {
}
