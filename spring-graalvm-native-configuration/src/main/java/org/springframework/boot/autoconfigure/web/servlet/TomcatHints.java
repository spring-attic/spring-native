package org.springframework.boot.autoconfigure.web.servlet;

import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http11.Http11NioProtocol;

import org.springframework.boot.autoconfigure.web.reactive.CommonWebInfos;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.graalvm.extension.MethodInfo;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger= Tomcat.class, typeInfos = {
		@TypeInfo(types = TomcatEmbeddedWebappClassLoader.class),
		@TypeInfo(types = AbstractProtocol.class, methods = @MethodInfo(name = "getLocalPort")),
		@TypeInfo(types = Http11NioProtocol.class)
}, importInfos = CommonWebInfos.class)
public class TomcatHints implements NativeImageConfiguration  {
}
