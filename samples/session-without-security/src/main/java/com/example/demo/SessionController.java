package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;

@Controller
public class SessionController {

	@PostMapping("/session")
	public String setAttribute(@ModelAttribute SessionAttributeForm sessionAttributeForm, WebSession session) {
		session.getAttributes().put(sessionAttributeForm.getAttributeName(), sessionAttributeForm.getAttributeValue());
		return "redirect:/";
	}

	@GetMapping("/")
	public String index(Model model, WebSession webSession) {
		model.addAttribute("webSession", webSession);
		return "home";
	}

	private static final long serialVersionUID = 2878267318695777395L;

}