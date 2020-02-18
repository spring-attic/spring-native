package org.springframework.web.bind.annotation;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

// TODO do these need to be more conditional? The triggers are either of the web stack configurations - do those
// autoconfigs share a common ancestor that these could trigger off? Although, of course these will only be exposed
// if they are on the classpath - why would you have the web jar on the classpath if not using these?
@ConfigurationHint(typeInfos = {
		@TypeInfo(types= {
				ResponseBody.class,RequestBody.class,RestController.class,
				RequestMapping.class,GetMapping.class,PostMapping.class},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS)
	})
public class Hints implements NativeImageConfiguration {
}
