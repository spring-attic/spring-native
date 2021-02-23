package org.apache.catalina;

import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http11.Http11NioProtocol;

import org.springframework.boot.autoconfigure.web.CommonWebInfos;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.nativex.hint.InitializationInfo;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.MethodInfo;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger= Tomcat.class, types = {
		@TypeInfo(types = TomcatEmbeddedWebappClassLoader.class),
		@TypeInfo(types = AbstractProtocol.class, methods = @MethodInfo(name = "getLocalPort"),access=AccessBits.CLASS),
		@TypeInfo(types = Http11NioProtocol.class),
}, initialization = {
		@InitializationInfo(types = {
				org.apache.catalina.servlets.DefaultServlet.class,
				org.apache.catalina.Globals.class
		}, initTime = InitializationTime.BUILD)
}, imports = CommonWebInfos.class)
public class TomcatHints implements NativeConfiguration {
}
