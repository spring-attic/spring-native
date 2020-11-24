package org.springframework.nativex;

import java.io.InputStream;

import org.junit.Test;

import org.springframework.nativex.domain.init.InitializationDescriptor;
import org.springframework.nativex.domain.init.InitializationJsonMarshaller;

public class InitTests {

	@Test
	public void checkInitClassesExist() throws Exception {
		InputStream s = this.getClass().getResourceAsStream("/initialization.json");
		InitializationDescriptor read = InitializationJsonMarshaller.read(s);
		read.getBuildtimeClasses().forEach(clazz -> {
			try {
				if(!clazz.equals("org.springframework.boot.SpringBootFactories")  // Substitution
				) {
					Class.forName(clazz);
				}
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});
		read.getRuntimeClasses().forEach(clazz -> {
			try {
				if (!clazz.equals("org.springframework.core.io.VfsUtils") // Dependency not available easily
						&& !clazz.equals("org.apache.tomcat.jni.SSL") // Require native library
						&& !clazz.equals("sun.reflect.misc.Trampoline") // Requires a specific class loader
				) {
					Class.forName(clazz);
				}
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
