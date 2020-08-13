package com.example.demo;

import static org.assertj.core.api.Assertions.*;

import com.example.test.TestServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;
import org.springframework.web.reactive.function.client.WebClient;

@FunctionalSpringBootTest({"spring.cloud.function.web.export.source.url=http://localhost:${export.port}/home",
    "spring.cloud.function.web.export.sink.url=http://localhost:${export.port}/echo",
    "logging.level.reactor=OFF",
    "logging.level.io.netty=OFF"})
public class DemoApplicationTests {

    static ConfigurableApplicationContext context;

    @Value("${export.port}")
    private int port;

    @Autowired
    private WebClient.Builder builder;

	@Test
	public void contextLoads() throws Exception {
        WebClient client = builder.baseUrl("http://localhost:" + port).build();
        client.post().uri("/add").bodyValue("Fred").exchange().block();
        Thread.sleep(100L);
        String response = client.get().uri("/take").exchange().block().bodyToMono(String.class).block();
        assertThat(response).isEqualTo("hi Fred!");
    }

    @AfterAll
    static void after() {
        if (context != null) {
            context.close();
        }
    }

    @BeforeAll
    static void before() {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("export.port", "" + port);
        context = SpringApplication.run(TestServer.class, "--server.port="+port, "--spring.cloud.function.web.export.enabled=false", "--spring.main.web-application-type=reactive");
    }

}
