package org.springframework.synthesizer.components;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/v1")
public interface ControllerSpecification {

    default ResponseEntity<String> ping(@RequestParam String input) {
        return ResponseEntity.ok("pong");
    }

}
