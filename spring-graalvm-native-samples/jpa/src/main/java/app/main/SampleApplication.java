package app.main;

import java.util.Optional;

import app.main.model.Foo;
import app.main.model.FooRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
public class SampleApplication {

	private FooRepository entities;

	public SampleApplication(FooRepository entities) {
		this.entities = entities;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
				Optional<Foo> foo = entities.findById(1L);
				if (!foo.isPresent()) {
					entities.save(new Foo("Hello"));
				}
		};
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(findOne()));
	}

	private Foo findOne() {
		return entities.findById(1L).get();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}
