package com.example.securingweb;

import java.security.Principal;

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
    public String anonymous() {
        return "anonymous";
    }

    @GetMapping("/authorized")
    public String authorized(Principal principal) {
        return "authorized: " + principal.getName();
    }

    @GetMapping("/admin")
    public String admin(Principal principal) {
        return "admin: " + principal.getName();
    }
}
