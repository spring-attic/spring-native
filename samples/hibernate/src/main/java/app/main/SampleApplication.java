package app.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.aot.thirdpartyhints.HikariRuntimeHints;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
@ImportRuntimeHints(HikariRuntimeHints.class)
public class SampleApplication {

	@PersistenceContext
	private EntityManager manager;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route().GET("/", request ->
				ok().body(manager.find(Foo.class, 1L))).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}

@Transactional
@Component
class Bootstrap implements CommandLineRunner {

	@PersistenceContext
	private EntityManager manager;

	@Override
	public void run(String... args) {
		Foo foo = manager.find(Foo.class, 1L);
		if (foo == null) {
			manager.persist(new Foo("Hello"));
		}
	}

}
