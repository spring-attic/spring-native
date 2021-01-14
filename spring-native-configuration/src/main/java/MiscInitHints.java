import org.springframework.nativex.extension.InitializationInfo;
import org.springframework.nativex.extension.InitializationTime;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;

@NativeImageHint(
		initializationInfos = @InitializationInfo(types = {
				org.h2.util.Bits.class,
				org.hibernate.EntityMode.class,
				javax.persistence.FetchType.class,
				javax.persistence.PersistenceContextType.class,
				javax.persistence.SynchronizationType.class,
				org.reactivestreams.Publisher.class,
				com.google.protobuf.Extension.class,
				com.google.protobuf.ExtensionLite.class,
				com.google.protobuf.ExtensionRegistry.class,
				com.rabbitmq.client.SocketChannelConfigurator.class,
				org.aopalliance.aop.Advice.class
		}, typeNames = {
				"org.springframework.cloud.function.web.function.FunctionEndpointInitializer"
		}, packageNames = {
				"org.springframework.data.r2dbc.connectionfactory",
				"io.r2dbc.spi",
				"javax.transaction"
		}, initTime = InitializationTime.BUILD))
public class MiscInitHints implements NativeImageConfiguration {
}
