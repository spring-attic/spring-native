package org.apache.catalina;

import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http11.Http11NioProtocol;

import org.springframework.boot.autoconfigure.web.CommonWebInfos;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.nativex.extension.InitializationInfo;
import org.springframework.nativex.extension.InitializationTime;
import org.springframework.nativex.extension.MethodInfo;
import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeHint(trigger= Tomcat.class, typeInfos = {
		@TypeInfo(types = TomcatEmbeddedWebappClassLoader.class),
		@TypeInfo(types = AbstractProtocol.class, methods = @MethodInfo(name = "getLocalPort"),access=AccessBits.CLASS),
		@TypeInfo(types = Http11NioProtocol.class),
}, initializationInfos = {
		@InitializationInfo(types = {
				org.apache.catalina.servlets.DefaultServlet.class,
				org.apache.catalina.Globals.class
		}, initTime = InitializationTime.BUILD)
}, importInfos = CommonWebInfos.class)
public class TomcatHints implements NativeConfiguration {
}
