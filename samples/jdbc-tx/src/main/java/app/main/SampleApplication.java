package app.main;

import app.main.model.Foo;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.thirdpartyhints.HikariRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.servlet.function.RouterFunction;

import static app.main.SampleApplication.*;
import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
@ImportRuntimeHints({ HikariRuntimeHints.class, Registrar.class })
public class SampleApplication {

	@Bean
	public RouterFunction<?> userEndpoints(Finder<Foo> entities) {
		return route(GET("/"), request -> ok().body(entities.find(1L)));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	static class Registrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			// TODO https://github.com/spring-projects/spring-boot/issues/31533
			hints.resources().registerPattern("schema.sql");

			hints.reflection().registerType(Foo.class,
					builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
		}
	}

}
