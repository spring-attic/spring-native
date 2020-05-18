package com.example.jafu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.fu.jafu.JafuApplication;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.fu.jafu.Jafu.webApplication;
import static org.springframework.fu.jafu.webmvc.WebMvcServerDsl.webMvc;

public class DemoApplication {

	public static JafuApplication app = webApplication(a -> {
		a.beans(b -> b.bean(CommandLineRunner.class, () -> arguments -> System.out.println("jafu running!")));
		a.enable(webMvc(s -> {
			s.port(8080);
			s.router(r -> {
				r.GET("/", req -> ServerResponse.ok().body("Hello"));
			});
		}));
	});

	public static void main(String[] args) throws InterruptedException {
		app.run(args);
	}

}