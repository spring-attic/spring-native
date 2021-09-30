package reactor.core.publisher;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = Traces.class, types = {
		@TypeHint(types = {
				Traces.StackWalkerCallSiteSupplierFactory.class,
				Traces.SharedSecretsCallSiteSupplierFactory.class,
				Traces.ExceptionCallSiteSupplierFactory.class
		})
})
public class ReactorCoreHints implements NativeConfiguration {
}
