package com.example.commandlinerunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@Conditional(TrickyCondition.class)
public class ConditionalConfig {
    
    @Bean
    SomeBean getBean() {
        return new SomeBean();
    }
    
}

