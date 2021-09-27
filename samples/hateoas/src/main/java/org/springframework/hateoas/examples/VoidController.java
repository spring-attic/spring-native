package org.springframework.hateoas.examples;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoidController {

	@GetMapping
	public void getNothing() {
	}
}
