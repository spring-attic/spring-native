package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@NativeHint(
		options = {
				// https://github.com/oracle/graal/issues/3875
				"-J--add-exports=java.management/sun.management=ALL-UNNAMED"
		}
)
@SpringBootApplication
public class DemoApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	
	@Bean
	CustomNoComponent getone() {
		return new CustomNoComponent();
	}

}

@RestController
class C {
	@GetMapping("/")
	public String h() {
		return "hello";
	}
}
