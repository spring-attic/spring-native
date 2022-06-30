package com.example.webflux;

import java.security.Principal;

import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Moritz Halbritter
 */
@RestController
@RequestMapping(path = "/rest", produces = MediaType.TEXT_PLAIN_VALUE)
public class TestRestController {
    @GetMapping("/anonymous")
    public Mono<String> anonymous() {
        return Mono.just("anonymous");
    }

    @GetMapping("/authorized")
    public Mono<String> authorized(Principal principal) {
        return Mono.just("authorized: " + principal.getName());
    }

    @GetMapping("/admin")
    public Mono<String> admin(Principal principal) {
        return Mono.just("admin: " + principal.getName());
    }
}
