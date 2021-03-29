package app.main;

import app.main.model.Flurb;
import app.main.model.Foo;
import app.main.model.FooRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.function.RouterFunction;

import java.util.Optional;

import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.*;
import static org.springframework.web.servlet.function.ServerResponse.*;

@SpringBootApplication
public class SampleApplication {

	private final FooRepository entities;

	public SampleApplication(FooRepository entities) {
		this.entities = entities;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {

			Optional<Foo> maybeFoo = entities.findById(1L);
			Foo foo;
			foo = maybeFoo.orElseGet(() -> entities.save(new Foo("Hello")));
			Flurb flurb = new Flurb();
			flurb.setValue("Balla balla");
			foo.setFlurb(flurb);
			entities.save(foo);

			entities.findWithBetween("a", "X");
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
