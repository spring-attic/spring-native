package com.example.webmvc;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class InterfaceDefinedController implements ControllerDefinition {

    @Override
    public Response echo(Request request) {
        return new Response(request.getMessage());
    }

}
