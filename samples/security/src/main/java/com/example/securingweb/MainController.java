package com.example.securingweb;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {
    @GetMapping("/")
    public String index() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

	@GetMapping("/hello")
	public ModelAndView hello(HttpServletRequest httpServletRequest) {
		return new ModelAndView("hello", Map.of("user", httpServletRequest.getRemoteUser()));
	}

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
