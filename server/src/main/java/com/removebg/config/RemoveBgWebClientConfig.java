package com.removebg.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RemoveBgWebClientConfig {

    @Bean
    public WebClient removeBgWebClient(
            @Value("${removebg.base-url}") String baseUrl,
            @Value("${removebg.api-key}") String apiKey ) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key",apiKey)
                .build();
    }
}
