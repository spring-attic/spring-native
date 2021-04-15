package com.example.validator;

import javax.validation.Valid;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

   private final ApplicationContext context;

    public TestController(ApplicationContext context) {
        this.context = context;
    }

    @GetMapping("/")
    public String form(ModelMap model) {
        model.addAttribute("form", new Form());
        return "form";
    }

    @PostMapping("/validateForm")
    @ResponseBody
    public String validateForm(@Valid Form form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "Validation failed: " + form.getTestIntMin();
        }
        return "Validation passed: " + form.getTestIntMin();
    }

}
