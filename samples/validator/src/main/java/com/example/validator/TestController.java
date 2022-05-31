/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.validator;

import jakarta.validation.Valid;

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
