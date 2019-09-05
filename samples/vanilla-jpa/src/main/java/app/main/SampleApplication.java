package app.main;

import java.util.Optional;

import app.main.model.Foo;
import app.main.model.FooRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication(proxyBeanMethods = false)
public class SampleApplication {

	private FooRepository entities;

	public SampleApplication(FooRepository entities) {
		this.entities = entities;
	}

	@Bean
	public CommandLineRunner runner() {
		System.err.println("+++++++++++");
		return args -> {
			try {
				System.err.println("****");
				Optional<Foo> foo = entities.findById(1L);
				System.err.println("****: " + foo);
				if (!foo.isPresent()) {
					entities.save(new Foo("Hello"));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(
				Mono.fromCallable(this::findOne).log().subscribeOn(Schedulers.elastic()),
				Foo.class));
	}

	private Foo findOne() {
		try {
			return entities.findById(1L).get();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}
