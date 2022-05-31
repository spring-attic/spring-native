package com.example.session;

import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
    @GetMapping("/")
    public String index() {
        return "home";
    }

    @PostMapping("/setValue")
    public String setValue(@RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "value", required = false) String value, HttpServletRequest request) {
        if (!ObjectUtils.isEmpty(key) && !ObjectUtils.isEmpty(value)) {
            request.getSession().setAttribute(key, value);
        }
        return "home";
    }

}
