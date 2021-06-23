package org.springframework.synthesizer.components;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller implements ControllerSpecification {

    @Override
    public ResponseEntity<String> ping(String input) {
        return ResponseEntity.ok(input);
    }

}
