package com.example.webmvc;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ControllerDefinition {

    @PostMapping(
            value = "/echo",
            produces = { "application/json" },
            consumes = { "application/json" }
    )
    Response echo(@RequestBody Request request);

}
