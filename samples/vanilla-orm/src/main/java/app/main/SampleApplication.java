package app.main;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;

import app.main.model.Foo;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication(proxyBeanMethods = false)
public class SampleApplication {

	@PersistenceContext
	private EntityManager manager;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"),
				request -> ok().body(Mono
						.fromCallable(
								() -> manager.find(Foo.class, 1L))
						.subscribeOn(Schedulers.elastic()), Foo.class));
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
	public void run(String... args) throws Exception {
		
		Foo foo = manager.find(Foo.class, 1L);
		if (foo == null) {
			manager.persist(new Foo("Hello"));
		}
	}

}