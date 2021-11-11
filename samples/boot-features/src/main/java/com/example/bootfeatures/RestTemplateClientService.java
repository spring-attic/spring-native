package com.example.bootfeatures;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateClientService {

    private final RestTemplate restTemplate;

    public RestTemplateClientService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String get(String url) {
        return this.restTemplate.getForObject(url, String.class);
    }

}
