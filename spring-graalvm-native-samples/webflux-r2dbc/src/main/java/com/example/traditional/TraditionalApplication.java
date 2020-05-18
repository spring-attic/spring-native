package com.example.traditional;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication(proxyBeanMethods = false)
public class TraditionalApplication {

	@Bean
	RouterFunction<ServerResponse> routes(ReservationRepository rr) {
		return route().GET("/reservations", r -> ok().body(rr.findAll(), Reservation.class)).build();
	}

	@Bean
	ApplicationRunner runner(DatabaseClient dbc, ReservationRepository reservationRepository) {
		return args -> {

			dbc.execute("create table reservation\n" + "(\n" + "    id   serial primary key,\n"
					+ "    name varchar(255) not null\n" + ")").fetch().rowsUpdated().subscribe();

			reservationRepository.save(new Reservation(null, "Andy")).subscribe();
			reservationRepository.save(new Reservation(null, "Sebastien")).subscribe();

			reservationRepository.findAll().subscribe(System.out::println);
			;
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(TraditionalApplication.class, args);
	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {

}

class Reservation {

	@Id
	private Integer id;
	private String name;

	public String toString() {
		return "Reservation(id="+id+",name="+name+")";
	}

	@Override
	public int hashCode() {
		return id * 37 + name.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof Reservation) {
			Reservation that = (Reservation) o;
			return this.id == that.id && this.name.equals(that.name);
		}
		return false;
	}

	public Reservation(Integer id, String name) {
		this.id = id;
		this.name = name;

	}

	public Reservation() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
